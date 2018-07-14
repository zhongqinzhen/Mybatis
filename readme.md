# 说明文档

我们在使用Mybatis的时候，一般都是搭配Spring进行直接使用，也就是我们直接面向的就是`*mapper.java`文件，定义好了相关的接口跟对应的`mapper.xml`文件之后，我们就可以通过相应的接口调用到找到相应的SQL文件进行执行，但是对程序而言，Mybatis对外的实际上不是`*mapper.java`文件，我们只是通过Spring帮忙管理注入了`*mapper.java`文件，而如果不通过Spring来做注入的操作，那么所有的`*mapper`接口实际上都是通过`SqlSession`来获取的；

我们之前说到，在获取对应的`Mapper`类时，实际上是通过`SqlSession`接口的`getMapper()`方法实现的，而该方法的内部实现实际上是委托给`Configuration.getMapper()`方法来实现的，最终的实现是通过`JDK`动态代理来实现的，而动态代理中的`InvocationHandler`实际上就是`MapperProxy`对象；

而在执行`Mapper`类的响应方法接口的时候，查看`MapperProxy`的相关实现，会发现实际上在执行CURD的时候，最终还是调用回`SqlSession`的相关CRUD的方法，而`SqlSession`实际上是委托给`Executor`的相关方法来执行的，`Executor`再通过构造`StatementHandler、ParameterHandler、ResultSetHandler`来拼凑参数，执行SQL，包装成ResultSet对象进行返回，实际上底层也还是通过建立`Connection、Statement`来进行相应的操作；

所以，简易版的Mybatis应该包含以下结构：

![image-20180711142105125](https://ws4.sinaimg.cn/large/006tKfTcgy1ft5wgk2t1oj317g0u2wie.jpg)

## 简易版实现V1

### 说明

在Mybatis实际实现中，是存在xml配置文件的，而配置文件的实际加载解析是在`DefaultSessionFactoryBuilder.builder()`方法中，在这里我们不进行xml文件解析，假定我们已经定位到Mapper文件的并加载了；而Mapper文件的相关SQL我们也不通过xml来指定，简单起见我们在`Mapper.java`的相关接口上面通过自定义注解的方式来实现，而`namespace`也是通过在`mapper`接口类的自定义注解来实现；

在上面的分析中，因为`SqlSession`实际上是对外的接口，而且最终是将`getMapper()`方法的具体实现委托给`Configuration`对象的，所以`SqlSession`是持有`Configuration`对象的；而`Configuration`实际上又是同构`JDK`的动态代理委托给`MapperProxy`的，且最终执行的CRUD操作都会通过`SqlSession`引用，所以在调用`getMapper()`方法的时候可以将`SqlSession`自身的引用通过`Configuration`传递给`MapperProxy`，最终在执行CRUD的时候，才可以调用回`SqlSession`类。

而`MapperProxy`的职责主要是通过对调用方法进行反射获取相关的注解对象，通过对注解对象的分析来决定要调用`SqlSession`的哪一个方法；而`SqlSession`来在执行某一个具体的CRUD操作的时候，实际上是委托给`Executor`对象的，所以`SqlSession`对应还需要持有`Executor`对象，再透过`Executor`对象来执行相应的SQL操作；这里暂时直接生硬的使用`JDK`的`Connection`和`Statement`来操作，没有封装成相应的`StatementHandler、ParameterHandler、ResultSetHandler`；

类图关系如下：

![image-20180711143619098](https://ws3.sinaimg.cn/large/006tKfTcgy1ft5wwgtyc5j31kw0uhn6o.jpg)

**简易版代码包：com.zqz.mybatis.customer.v1**

###关键代码

**MapperProxy.invoke**

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    /** 获取方法对应的类*/
    Class clazz = method.getDeclaringClass();
    /** 获取类上面指定的namespace注解*/
    Namespace namespace = (Namespace) clazz.getAnnotation(Namespace.class);

    /** 如果类上的namespace跟类的路径不一致，直接调用相应的方法
     *  这里只是为了仿照Mybatis上namespace的校验，实际并不是这么用的
     *
     *  实际上Mybatis是在映射MapperProxy的时候就已经继续校验了，
     *      而校验逻辑则是调用getMapper方法时检查该Class是否有指定的MapperProxyFactory
     *      而添加逻辑这是在进行mapper.xml解析的时候，有一步绑定namespace的时候构建相应的MapperProxyFactory
     *      实际上就是将Class跟MapperProxyFactory通过Map对象映射起来
     *
     *  如果namespace校验通过，则判断方法上的注解，执行不同的CRUD操作
     * */
    if (namespace != null && namespace.value().equalsIgnoreCase(clazz.getName())){
        Annotation[] annotations = method.getAnnotations();
        String sqlStatement = null;
        Annotation annotation = annotations[0];
        if (annotation instanceof Select){
            sqlStatement = ((Select)annotation).value();
            return sqlSession.selectOne(sqlStatement, String.valueOf(args[0]));
        } else if (annotation instanceof Insert){
            sqlStatement = ((Insert)annotation).value();
            return sqlSession.insert(sqlStatement,args[0]);
        }
        return null;
    }
    return method.invoke(proxy,args);
}
```

## 简易版实现V2

### 说明

在版本V1中，虽然我们提到了`StatementHandler`跟`ResultSetHandler`，但是实际上我们都是在`Executor`类中直接通过`Connection`跟`Statement`来操作数据库的，而且`Mybatis`中的查询是存在一级缓存的，而V1实际上是每次都会去查找`DB`返回具体数据的；

所以在V2版本中，我们来完善Mybatis的其他几个功能

* 扫描包，根据扫描到的信息把相关属性记录到`Configuration`对象中，同时在获取`Mapper`对象的时候校验`namespace`
* 实现`CacheExecutor`类，也就是实现`SqlSession`的一级缓存操作
* 将`Executor`中的操作拆离到`StatementHandler`、`ResultSetHandler`中去

**代码包：com.zqz.mybatis.customer.v2**

#### 扫描包

添加扫描包的功能其实就是通过在启动前将指定包路径的类进行扫描，将相关`Mapper`类的注解信息添加`Configuration`对象中，并在获取`Mapper`类接口的时候进行校验；在实现中，我们实际上是把指定的包路径下的所有文件都进行了扫描判断，但是实际在`Mybatis`中，**会判断扫描的是不是接口且是独立的（没有实现类）**，如果是才会进行下一步判断，否则的话直接跳过；

#### 扫描包部分代码

```
private static CustomerConfigurationV2 scanMapperLocation(String scanPath){
    Reflections reflections = new Reflections(scanPath);

    CustomerConfigurationV2 customerConfigurationV2 = new CustomerConfigurationV2();

    /** 查找所有以Namespace为类注解的类，如果没有用Namespace注解，代表不是要扫描的类*/
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Namespace.class);

    for (Class clazz : classes){
        Namespace namespace = (Namespace) clazz.getAnnotation(Namespace.class);
        try {
            /** 添加绑定关系，但是如果配置的值无法反射成Class类，忽略*/
            Class mapperClazz = Class.forName(namespace.value());
            customerConfigurationV2.addMapper(mapperClazz);

        } catch (ClassNotFoundException e) {
            continue;
        }

        Method[] methods = clazz.getMethods();

        for (Method method : methods){
            Annotation annotation = method.getAnnotations()[0];

            String fullyMethodName = clazz.getName() + "." + method.getName();
            if (annotation instanceof Insert){
                Insert insert = (Insert) annotation;
                customerConfigurationV2.addMapperData(fullyMethodName,buildMapperData(insert.value(),null,CRUDEnum.INSERT));
                continue;
            } else if(annotation instanceof Select){
                Select select = (Select) annotation;
                customerConfigurationV2.addMapperData(fullyMethodName,buildMapperData(select.value(),
                        method.getReturnType(),CRUDEnum.SELECTONE));
                continue;
            }
        }
    }

    return customerConfigurationV2;
}

private static MapperData buildMapperData(String sql, Class resultType, CRUDEnum crudEnum){
    MapperData mapperData = new MapperData();
    mapperData.setStatementSql(sql);
    mapperData.setResultType(resultType);
    mapperData.setCrudEnum(crudEnum);
    return mapperData;
}
```

#### Executor实现

在实现中，会将V1中的`Executor`类抽象成接口，然后实现成两个具体的实现类`SimpleExecutor`和`CacheExecutor`，很明显，`CacheExecutor`类是带有缓存的方法，也就是在实际的执行过程中，会判断内存中是否已经存在，如果存在，那么就直接返回不进行相应的操作；但是如果不存在，那么就会调用`SimpleExecutor`去操作；所以很明显，这里`CacheExecutor`类会持有一个`Executor`对象，而且`CacheExecutor`类的职责只有一个，就是判断内存中是否存在，存在就直接返回；

> 可以理解成装饰器设计模式

#### Executor实现代码

```
package com.zqz.mybatis.customer.v2.executor;


import com.zqz.mybatis.customer.v2.configuration.MapperData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class CacheExecutor implements Executor{

    private Executor delagate;

    private Map<String,Object> cacheMap = new HashMap<>();
    
    public CacheExecutor() {
        delagate = new SimpleExecutor();
    }
    
    @Override
    public <T> T selectOne(MapperData mapperData, String parameter) {
        if (cacheMap.containsKey(mapperData.getStatementSql().concat(parameter))){
            return (T) cacheMap.get(mapperData.getStatementSql().concat(parameter));
        }
        Object object = delagate.selectOne(mapperData, parameter);
        cacheMap.put(mapperData.getStatementSql().concat(parameter),object);
        return (T) object;
    }
    
    @Override
    public int insert(MapperData mapperData, Object parameter) {
        return delagate.insert(mapperData,parameter);
    }
}
```

#### 拆离Executor

拆离`Executor`主要是指将数据库的相关操作隔离到`StatementHandler`跟`ResultSetHandler`中

* `StatementHandler`主要负责构建操作数据库的连接并执行相关的SQL操作
* `ResultSetHandler`主要是负责将`select`语句查询结构后的`ResultSet`对象构造成我们想要的结果对象

所以很明显，`StatementHandler`类主要就是负责`Connection`跟`Statement`的操作；实际在`Mybatis`的实现中，还会构建一个`ParameterHandler`对象，用来构建数据库执行参数的；而`ResultSetHandler`主要就是构造目标结果对象的，在实现的时候，我是通过构建一个`handlerMap`对象来标记实体对象属性跟数据库对象属性的名字的，而`Mybatis`中是通过`TypeHandler`接口来实现的，实际上每个不同的数据类型会构建一个不同的`TypeHandler`来实现；

####拆离Executor部分代码实现

`StatementHandler`

```
public <T> List<T> query(String statementSql, String parameter, Class resultType){
    Connection connection = getConnection();
    Statement statement = getStatement(connection);

    List<T> tlist = new ArrayList<>();
    try {
        ResultSet resultSet = statement.executeQuery(String.format(statementSql, Integer.parseInt(parameter)));

        tlist.add((T) resultSetHandler.handler(resultSet,resultType));

        return tlist;

    } catch (Exception e) {
        e.printStackTrace();
    } finally {

        closeConnection(connection);

        closeStatement(statement);
    }
    return null;
}
```

`ResultSetHandler`

```
public class ResultSetHandler {
    private static Map<String,String> handlerTypeMap = new HashMap<>();

    static {
        handlerTypeMap.put("id","id");
        handlerTypeMap.put("tagName","tag_name");
        handlerTypeMap.put("tagType","tag_type");
        handlerTypeMap.put("createTime","create_time");
        handlerTypeMap.put("updateTime","update_time");
    }

    public <T> T handler(ResultSet resultSet, Class resultType) {

        try {
            Object obj = resultType.newInstance();

            if (resultSet.next()){
                for (Field field : resultType.getDeclaredFields()){
                    setValue(obj,field,resultSet,resultType);
                }
            }
            return (T) obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setValue(Object obj,Field field,ResultSet resultSet,Class resultType){
        String fieldName = field.getName();

        String methodName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1,field.getName().length());

        try {

            Object dbVal = resultSet.getObject(handlerTypeMap.get(fieldName));

            resultType.getMethod(methodName,field.getType()).invoke(obj,dbVal);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
```

> 以上这种查询是肯定有问题的，因为数据库的类型跟Java实体的类型不匹配，无法通过set反射设置进去的。在这里数据库是java.math.BigInteger，而实体类是java.lang.Long。所以会报异常信息：java.lang.IllegalArgumentException: argument type mismatch

#### Plugin功能

在Mybatis中，是可以配置`plugin`插件来对某一个点的功能进行拦截操作的，类似于Spring的AOP，目前Mybatis支持拦截的方法调用有

* Executor（update、query、flushStatements、commit、rollback、getTransaction、close、isClosed）
* ParameterHandler（getParameterObject、setParameters）
* ResultSetHandler（handlerResultSets、handlerOutputParameters）
* StatementHandler（prepare、parameterize、batch、update、query）

也就是在具体类的某个方法执行时，Mybatis可以根据自定义的插件配置来添加执行逻辑；

在Mybatis实现中，会在`Configuration`中存储一个`InterceptorChain`属性来记录我们配置了的插件，Mybatis通过解析xml的`plugins`属性，将相关的插件配置信息添加到`Configuration.interceptorChain`中；

```java
pluginElement(root.evalNode("plugins"));

private void pluginElement(XNode parent) throws Exception {
    if (parent != null) {
        for (XNode child : parent.getChildren()) {
            String interceptor = child.getStringAttribute("interceptor");
            Properties properties = child.getChildrenAsProperties();
            Interceptor interceptorInstance = (Interceptor) 
                resolveClass(interceptor).newInstance();
            interceptorInstance.setProperties(properties);
            configuration.addInterceptor(interceptorInstance);
            
        }
    }
}
```

而在Mybatis实际实例化获取`Executor（Configuration.newExecutor）`、`ParameterHandler（Configuration.newParameterHandler）`、`StatementHandler（Configuration.newStatementHandler）`、`ResultSetHandler（Configuration.newResultSetHandler）`的时候，会执行`各个插件Interceptor的plugin`方法，而这个方法由用户自定义实现；Mybatis实现了一个`Plugin.wrap`方法，这个方法是通过解析自定义`Interceptor`的相关注解信息`Signature`，然后通过`JDK`的动态代理代理了具体对象，这里是`Executor、ParameterHandler、StatementHandler、ResultSetHandler`的子类对象，然后再这些具体对象执行相应的方法的时候，通过判断`Interceptor`是否拦截了该方法来决定是否调用相应的`intercept`方法；

大概时序图如下

![image-20180714160217496](https://ws4.sinaimg.cn/large/006tNc79gy1ft9g8rxa3cj31kw0sl44l.jpg)

在v2版本中，也模拟了一个插件的功能，插件的功能为只要当前执行的类跟方法跟我们配置的一致，即在方法执行前执行插件的功能；类一致是指当前运行的类为配置类的子类；

插件的信息不通过解析xml信息获取，而是通过自定义注解获取；并将插件的信息设置到`CustomerConfigurationV2`中;

```java
private static void scanCustomerInterceptor(String scanPath,CustomerConfigurationV2 customerConfigurationV2){
    Reflections reflections = new Reflections(scanPath);

    /** 查找所有以CustomerPlugins为类注解的类，如果没有用CustomerPlugins注解，代表不是要扫描的类*/
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CustomerPlugins.class);

    for (Class clazz : classes){
        try {
            customerConfigurationV2.addInterceptor((CustomerInterceptor) clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

然后在`Executor`中执行CRUD操作的时候，进行插件验证并执行的操作，因为这个时候需要知道插件的信息，所以在`SimpleExecutor`中持有了一个`CustomerConfigurationV2`对象；验证并执行插件的代码如下：

```java
private void doPlugin(String methodName){
    for (CustomerInterceptor interceptor : configuration.getInterceptorList()){
        CustomerPlugins annotation = interceptor.getClass().getAnnotation(CustomerPlugins.class);
        if (annotation == null){
            continue;
        }
        if (methodName.equals(annotation.method()) &&
                annotation.clazzType().isAssignableFrom(this.getClass())){
            interceptor.executor();
        }
    }
}
```

具体实现为只要我们自定义注解的`methodName`跟当前要仔细的CRUD的名称一致，且`clazzType`是当前执行类的父类或者父接口，那么就执行插件的`executor`方法；

所以现在V2版本的自定义Mybatis中的结构为

![image-20180714161335211](https://ws1.sinaimg.cn/large/006tNc79gy1ft9gkgja95j311y10idi6.jpg)

