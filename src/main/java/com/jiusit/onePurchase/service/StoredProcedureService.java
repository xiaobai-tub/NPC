package com.jiusit.onePurchase.service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.ICallback;
import com.jiusit.common.utils.DateUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class StoredProcedureService implements ICallback{
		public String ticket_id = null;
	    public String from_user=null;
	    public String user_id=null;
	    public String user_name=null;
	    public String get_type=null;
	    public String card_type=null;
	    public String begin_date=null;
	    public String end_date=null;
	    BigDecimal price=new BigDecimal("0");
	    int result;
	    public String reason="数据库意外,请重试";
	    CallableStatement proc = null;
		@Override
		public Object call(Connection conn) throws SQLException {
			// TODO Auto-generated method stub
		    
	           try {           
	             proc = (CallableStatement) conn.prepareCall("{ call give_ticket(?,?,?,?,?,?,?,?,?,?) }"); // borrow为mysql的存储过程名，其中有两个参数，两个返回值
	             proc.setString(1, ticket_id);//设置参数值
	             proc.setBigDecimal(2, price);
	             proc.setString(3, from_user);
	             proc.setString(4, user_id);
	             proc.setString(5, user_name);
	             proc.setString(6, get_type);
	             proc.setString(7, begin_date);
	             proc.setString(8, end_date);
	             proc.setString(9, card_type);
	             proc.registerOutParameter(10, java.sql.Types.INTEGER);//设置返回值类型
	             proc.execute();
	             result =  proc.getInt(10);//得到返回值
	           // reason=proc.getString(4);
	      }catch(Exception e){
	              e.printStackTrace();
	       } finally {
	           // DbKit.close(proc, conn);
	    	   proc.close();
	    	   conn.close();
	          }
			return null;
		}
		
	//调用内部类方法
	  public  static int trackresult(String ticket_id,
	    String from_user,
	    String user_id,
	    String user_name,
	    BigDecimal price,String get_type,int iField,int iValue,String card_type){//可以加参数 card_type 卡劵类型
		  String beginDate=DateUtil.formatDate(new Date(), "yyyy-MM-dd");
		  String endDate=DateUtil.formatDate(DateUtil.dateAdd(iField, iValue, new Date()), "yyyy-MM-dd");
		  StoredProcedureService  storedProcedureService =new StoredProcedureService();
		  storedProcedureService.ticket_id=ticket_id;
		  storedProcedureService.from_user=from_user;
		  storedProcedureService.user_id=user_id;
		  storedProcedureService.user_name=user_name;
		  storedProcedureService.price=price;
		  storedProcedureService.get_type=get_type;
		  storedProcedureService.begin_date=beginDate;
		  storedProcedureService.end_date=endDate;
		  storedProcedureService.card_type=card_type;
	             Db.execute(storedProcedureService);
	            int result=storedProcedureService.result;  
	            return result;
	  }
	
	  public static void main(String args[]) { 
		  //trackresult("450305cb10944a0cb1e9798bc578bcf8","oOppcuHy3biBj9UyRN-N-v-Cr2Sw","52005aac0d444a14b90d8e3e321e204b","wlanzy",10,"03");
	  } 
}
