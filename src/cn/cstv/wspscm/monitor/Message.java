package cn.cstv.wspscm.monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
	
	private String messageOrigin;

	/**
	 * @return the messageOrigin
	 */
	public String getMessageOrigin() {
		return messageOrigin;
	}

	/**
	 * @param messageOrigin the messageOrigin to set
	 */
	public void setMessageOrigin(String messageOrigin) {
		this.messageOrigin = messageOrigin;
	}
	
	//////////////////////////////////////////////////////////////////
	//�µ���Ϣ��ʽ��
	//2010-10-07 14:09:21.687 [Gps Call]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.getHeading(255):90
	//
	//
	/////////////////////////////////////////////////////////////////

	private String messageType; // ��Ϣ���ͣ�����invoke, on message, receive etc.

	private String messageFunction; // ��Ϣ��������

	private String parameter; // ��Ϣ��������
	
	private String returnValue;//����ֵ����

	private String messageFullText; // ��Ϣȫ��ʽ�����硰[on
									// Message]detectVitalParameters(vitalParameters)��

	private boolean messageStatus; // ��Ϣ״̬��true��ʾ��Entering��, false��ʾ��Exiting��

	private String timedCondition; // ��Ϣʱ�����������硰2008-04-17 13:00 ��
	
//	private int x = -1;            //��Ϣ�����е�xֵ�÷�Χ
//	
//	private int xSymbol = -1;      //0��ʾ<, 1��ʾ=, 2��ʾ>, 3��ʾ:=
//	
//	private int y = -1;            //��Ϣ�����е�yֵ�÷�Χ
//	
//	private int ySymbol = -1;       //0��ʾ<, 1��ʾ=, 2��ʾ>, 3��ʾ:=
	
	private double time = -1.0;            //��Ϣ������ʱ��


	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	public void setTime(String firstTime) {
		//2008-04-17 13:00 
		//2008-04-19 07:00
		if(firstTime.contains("-")){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			try {
				Date firstdate = formatter.parse(firstTime);
			   Date mydate = formatter.parse(timedCondition); 
			   setTime((mydate.getTime() - firstdate.getTime()) / (1000));	//����Ϊ��λ 
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		else{
			Double first = Double.parseDouble(firstTime);
			setTime(Double.parseDouble(timedCondition)-first);
		}

	}

	public String getMessageFullText() {
		return messageFullText;
	}

	public void setMessageFullText(String messageFullText) {
		this.messageFullText = messageFullText;
	}

	public void setMessageFullText() {
		messageFullText = messageFunction+ "(" + parameter + ")";
//		if( returnValue.length() == 0)
//		{
//			this.messageFullText = "[" + messageType + "]" + messageFunction + "(" + parameter + ")" ;			
//		}
//		else
//		{
//			this.messageFullText = "[" + messageType + "]" + messageFunction+ "(" + parameter + "):"+returnValue;		
//		}
	}

	public Message() {

	}

	public Message(String timedCondition,String type, String messageText, String parameter, String returnValue) {
		this.messageStatus = true;
		this.timedCondition = timedCondition;
		this.messageType = type;
		this.messageFunction = messageText;
		this.parameter = parameter;
		this.returnValue = returnValue;
		this.messageFullText = messageText+ "(" + parameter + ")";
//		if( returnValue.length() == 0)
//		{
//			this.messageFullText = "[" + type + "]" + messageText+ "(" + parameter + ")";			
//		}
//		else
//		{
//			this.messageFullText = "[" + type + "]" + messageText+ "(" + parameter + "):"+returnValue;		
//		}
//		setTime(timedCondition);
//		System.out.println(getTime());
	}

	public String getType() {
		return messageType;
	}

	public void setType(String type) {
		this.messageType = type;
	}

	public String getMessageText() {
		return messageFunction;
	}

	public void setMessageText(String messageText) {
		this.messageFunction = messageText;
	}

	public String getMessageFunction() {
		return messageFunction;
	}

	public void setMessageFunction(String messageFunction) {
		this.messageFunction = messageFunction;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public boolean isMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(boolean messageStatus) {
		this.messageStatus = messageStatus;
	}
	
	//�����ж�������String��������ʼ����Ϣ״̬
	public void setMessageStatus(String messageStatus) {
		if(messageStatus.equals("Entering")){
			this.messageStatus = true;
		}
		else if(messageStatus.equals("Exiting")){
			this.messageStatus = false;
		}
		else{
			System.out.println("Wrong messae status");
		}
		
	}

	public String getTimedCondition() {
		return timedCondition;
	}

	public void setTimedCondition(String timedCondition) {
		this.timedCondition = timedCondition;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}

	public String getReturnValue() {
		return returnValue;
	}

//	public int getX() {
//		return x;
//	}
//
//	public void setX(int x) {
//		this.x = x;
//	}
//
//	public int getXSymbol() {
//		return xSymbol;
//	}
//
//	public void setXSymbol(int symbol) {
//		xSymbol = symbol;
//	}
//
//	public int getY() {
//		return y;
//	}
//
//	public void setY(int y) {
//		this.y = y;
//	}
//
//	public int getYSymbol() {
//		return ySymbol;
//	}
//
//	public void setYSymbol(int symbol) {
//		ySymbol = symbol;
//	}

}
