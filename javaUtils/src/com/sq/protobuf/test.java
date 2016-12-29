package com.sq.protobuf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sq.protobuf.DocumentProtos.Entity;
import com.sq.protobuf.DocumentProtos.Relation;
import com.sq.protobuf.FirstProtobuf;
import com.sq.protobuf.DocumentProtos;



public class test {
	public static void main(String[] args) throws IOException {
		
		//���л�����
		//FirstProtobuf������������֣���proto�ļ��е�java_outer_classname
		//testBuf������ĳ�����е����֣���proto�ļ��е�message testBuf
		FirstProtobuf.testBuf.Builder builder=FirstProtobuf.testBuf.newBuilder();
		builder.setID(777);
		builder.setUrl("shiqi");
		
		//testBuf
		FirstProtobuf.testBuf info=builder.build();
		
		byte[] result = info.toByteArray() ;
		//System.out.println(result);
		
		try {
			FirstProtobuf.testBuf testBuf = FirstProtobuf.testBuf.parseFrom(result);
			System.out.println(testBuf);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		FileInputStream input = null;
		try {
			input = new FileInputStream("E://ig517//protobufdir//ecml//nyt-2005-2006.backup//1638728.xml.pb");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DocumentProtos.Document docdb = DocumentProtos.Document.parseFrom(input);
		System.out.println(docdb.getSentences(0));
		// ���ļ�ϵͳ�е�ĳ���ļ��л�ȡ�ֽ�
		
		 
		input = new FileInputStream("E://ig517//protobufdir//ecml//heldout_relations//testPositive.pb");
		System.out.println(input);
		String str="";
		String str1="";
	    InputStreamReader isr = new InputStreamReader(input);// InputStreamReader ���ֽ���ͨ���ַ���������,
	    BufferedReader br = new BufferedReader(isr);// ���ַ��������ж�ȡ�ļ��е�����,��װ��һ��new InputStreamReader�Ķ���
	    Integer i = 0;
	    while(true){
	    	Relation entdb = Relation.parseDelimitedFrom(input);
	    	System.out.println(i);
	    	System.out.println(entdb);
	    	if(i>=10) break;
	    	i++;
	    }
	    
		
	}
}

