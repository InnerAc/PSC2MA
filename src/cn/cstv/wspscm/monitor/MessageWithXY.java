package cn.cstv.wspscm.monitor;

public class MessageWithXY {

	private String messageType; // ��Ϣ���ͣ�����invoke, on message, receive etc.

	private String messageFunction; // ��Ϣ��������

	private String parameter; // ��Ϣ��������

	private String messageFullText; // ��Ϣȫ��ʽ�����硰[on
									// Message]detectVitalParameters(vitalParameters)��

	private boolean messageStatus; // ��Ϣ״̬��true��ʾ��Entering��, false��ʾ��Exiting��

	private String timedCondition; // ��Ϣʱ�����������硰x<1,y:=0��
	
	private int x = -1;            //��Ϣ�����е�xֵ�÷�Χ
	
	private int xSymbol = -1;      //0��ʾ<, 1��ʾ=, 2��ʾ>, 3��ʾ:=
	
	private int y = -1;            //��Ϣ�����е�yֵ�÷�Χ
	
	private int ySymbol = -1;       //0��ʾ<, 1��ʾ=, 2��ʾ>, 3��ʾ:=
	
	private double time = -1.0;            //��Ϣ������ʱ��


	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public String getMessageFullText() {
		return messageFullText;
	}

	public void setMessageFullText(String messageFullText) {
		this.messageFullText = messageFullText;
	}

	public void setMessageFullText() {
		this.messageFullText = "[" + messageType + "]" + messageFunction + "(" + parameter + ")" ;
	}

	public MessageWithXY() {

	}

	public MessageWithXY(String type, String messageText, String parameter) {
		this.messageType = type;
		this.messageFunction = messageText;
		this.parameter = parameter;
		this.messageFullText = "[" + type + "]" + messageText+ "(" + parameter + ")";
		this.time = -1.0;
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

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getXSymbol() {
		return xSymbol;
	}

	public void setXSymbol(int symbol) {
		xSymbol = symbol;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getYSymbol() {
		return ySymbol;
	}

	public void setYSymbol(int symbol) {
		ySymbol = symbol;
	}

}
