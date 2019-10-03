package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class HelloController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/hello")
    public String hello(){
        redisTemplate.opsForValue().set("version","0");

        Thread t=new Thread(()-> {
            redisTemplate.execute(new SessionCallback<Object>() {

                public Object execute(RedisOperations redisOperations) throws DataAccessException {
                    redisOperations.watch("version");

                    redisOperations.multi();
                    System.out.println(redisOperations.opsForValue().get("count"));
                    redisOperations.opsForValue().increment("count");
                    redisOperations.opsForValue().increment("version");
                    redisOperations.exec();

                    return null;
                }
            });
        });

        ExecutorService executorService= Executors.newFixedThreadPool(10);
        for(int i=0;i<100;i++){
            executorService.submit(t);
        }

        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return redisTemplate.opsForValue().get("count");
    }

    @RequestMapping("/hello2")
    public String hello2(){
        Runnable r=()->{
            synchronized(this){
                if(redisTemplate.opsForValue().get("count 2")==null){
                    redisTemplate.opsForValue().set("count 2","1");
                }else {
                    redisTemplate.opsForValue().increment("count 2");
                }
            }
        };

        ExecutorService executorService=Executors.newFixedThreadPool(10);
        for(int i=0;i<100;i++){
            executorService.submit(r);
        }

        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return redisTemplate.opsForValue().get("count 2");
    }
}
