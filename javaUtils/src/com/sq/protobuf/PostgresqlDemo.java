package com.sq.protobuf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javassist.util.HotSwapper;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 
 * @author wujs
 * @postgresql 密码验证
 解决方法： 
!、打开Postgresql安装目录下的data文件夹，找到pg_hba.conf文件并打开 
在# TYPE DATABASE    USER        CIDR-ADDRESS          METHOD的下面： 
加上一句： 
host all all    0.0.0.0/0    md5 (trust)

2、更改postgresql.conf下 
#listen_addresses = 'localhost' # what IP address(es) to listen on; 
为 
listen_addresses = '*' # what IP address(es) to listen on; 
记得去掉listen_addresses前的#号
 **/


public class PostgresqlDemo {
	static String hostname = "10.1.1.27";
	static Integer port = 5432;
	static Integer maxCon=4;
	static String username = "dbuser";
	static String password = "";
	static String databasename="exampledb";
	static String dbId="DB_AIDA";
	
	public static Properties getDatabaseProperties(
		      String hostname, Integer port, String username, String password,
		      Integer maxCon, String database) {
		    Properties prop = new Properties();
		    prop.put("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
		    prop.put("maximumPoolSize", maxCon);
		    prop.put("dataSource.user", username);
		    prop.put("dataSource.password", password);
		    prop.put("dataSource.databaseName", database);
		    prop.put("dataSource.serverName", hostname);
		    prop.put("dataSource.portNumber", port);
		    return prop;
		    
	}
	private static void initDataSource() {
		
	    //HikariConfig config = new HikariConfig();
	    Properties prop =  getDatabaseProperties(hostname,port,username,password,maxCon,databasename);
	    HikariConfig config = new HikariConfig(prop);
	    
	    System.out.println("finished...");
	    HikariDataSource ds = new HikariDataSource(config);
	    try {
			Connection connection = ds.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
        System.out.println("right...");
        
	}
	public static void main(String[] args){
		initDataSource();
	}
}
