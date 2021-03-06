package com.guo.codecraft.tinyioc.beans.facroty;

import com.guo.codecraft.tinyioc.BeanReference;
import com.guo.codecraft.tinyioc.aop.BeanFactoryAware;
import com.guo.codecraft.tinyioc.beans.BeanDefinition;
import com.guo.codecraft.tinyioc.beans.PropertyValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by guo on 3/1/2018.
 * 可自动装配内容的BeanFactory
 */
public class AutowireCapableBeanFactory extends AbstractBeanFactory {
    protected void applyPropertyValues(Object bean, BeanDefinition mbd) throws Exception {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
        for (PropertyValue propertyValue : mbd.getPropertyValues().getPropertyValues()) {
            Object value = propertyValue.getValue();
            //获取bean的引用类名
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) value;
                //这里返回的是引用对象的实例，即先初始化第A类，如果A类关联B类，就去找到
                value = getBean(beanReference.getName());
            }

            //引用的关联，这里先找有没有setXX()，即设置其他类引用的方法，如果有就调用这个方法，
            //如果没有就看有没有其他类引用的字段，如果有就直接引用把设置到对应的字段
            try {
                Method declaredMethod = bean.getClass().getDeclaredMethod(
                        "set" + propertyValue.getName().substring(0, 1).toUpperCase()
                                + propertyValue.getName().substring(1), value.getClass());
                declaredMethod.setAccessible(true);

                //直接调用bean的方法setXXX(类名)方法
                declaredMethod.invoke(bean, value);
            } catch (NoSuchMethodException e) {
                Field declaredField = bean.getClass().getDeclaredField(propertyValue.getName());
                declaredField.setAccessible(true);
                declaredField.set(bean, value);
            }
        }
    }
}
