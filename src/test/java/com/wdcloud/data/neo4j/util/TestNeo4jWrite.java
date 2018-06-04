package com.wdcloud.data.neo4j.util;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

public class TestNeo4jWrite{

    Driver driver;

    public TestNeo4jWrite(String uri, String user, String password){

        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    private void addPerson(){


        try {
            Session session = driver.session();
            try{

                String name = "";
                String title = "";
                String profile = "";

                Transaction tx = session.beginTransaction();
                for(int i = 100 ; i>=0; i --){

                    name =  "name" + String.format("%09d", i);
                    title = "title" + String.format("%09d", i);
                    profile = "profile" + String.format("%09d", i);

                     tx.run("Create (a:Neo4jPress{name:'name',title:'title',profile:'profile'})",
                             parameters("name", name,"title",title,"profile",profile));

                 }
                tx.success();
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void printPeople(){

        try{
            Session session = driver.session();
            StatementResult result = session.run(
                    "MATCH (a:JmeterTest) WHERE a.id = {x}  RETURN a.name AS name",parameters("x", "Start000000000'"));


            while (result.hasNext()){
                Record record = result.next();
                System.err.println(record.get("name").asString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        driver.close();
    }

    public static void main(String... args){

        //TestNeo4jWrite example = new TestNeo4jWrite("bolt://192.168.6.89:7687", "neo4j", "password");
        TestNeo4jWrite example = new TestNeo4jWrite("bolt://localhost:7687", "neo4j", "password");

        //example.printPeople();
        example.addPerson();

        /*for(int i = 1000000 ; i>=0; i --){
            name =  "name" + String.format("%09d", i);
            title = "title" + String.format("%09d", i);
            profile = "profile" + String.format("%09d", i);
        }*/

        example.close();
    }
}