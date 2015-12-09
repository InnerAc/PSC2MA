package cn.cstv.wspscm.monitor;

public class TimedAutomata {
	
	private String timedCondition; // ��Ϣʱ�����������硰x<1,y:=0��
	
	private String startStatus; //��ʼ״̬

	private String endStatus;   //����״̬
	
	private boolean yesORno;  //��Ϣ�ǣ�����Ϣ��
	
	private String automataMessage; //��Ϣ�����ı�����empty���������
	
	private int x = -1;            //��Ϣ�����е�xֵ�÷�Χ
	
	private int xSymbol = -1;      //0��ʾ<, 1��ʾ=, 2��ʾ>, 3��ʾ:=
	
	private int y = -1;            //��Ϣ�����е�yֵ�÷�Χ
	
	private int ySymbol = -1;       //0��ʾ<, 1��ʾ=, 2��ʾ>, 3��ʾ:=
	
	private int messageType=0;		//0��ʾ�ڲ���Ϣ��1��ʾ���ͣ�������2��ʾ���գ�����				tjf 20110915


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

	public TimedAutomata(){}
	
	public TimedAutomata(String startStatus,boolean yesORno,String automataMessage,String timedCondition,String endStatus){
		this.startStatus = startStatus;
		this.yesORno = yesORno;
		//System.out.println("TimedAutomata YESORNOLIST"+ isYesORno()+yesORno);
		this.automataMessage = automataMessage;
		this.timedCondition = timedCondition;
		this.endStatus = endStatus;
	}
	
	public TimedAutomata(String startStatus,boolean yesORno,String automataMessage,String timedCondition,String endStatus,int messageType){//tjf 20110921
		this(startStatus,yesORno,automataMessage,timedCondition,endStatus);
		this.messageType=messageType;
	}
	
	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getStartStatus() {
		return startStatus;
	}

	public void setStartStatus(String startStatus) {
		this.startStatus = startStatus;
	}

	public String getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(String endStatus) {
		this.endStatus = endStatus;
	}

	public boolean isYesORno() {
		return yesORno;
	}

	public void setYesORno(boolean yesORno) {
		this.yesORno = yesORno;
	}

	public String getAutomataMessage() {
		return automataMessage;
	}

	public void setAutomataMessage(String automataMessage) {
		this.automataMessage = automataMessage;
	}

	public String getTimedCondition() {
		return timedCondition;
	}

	public void setTimedCondition(String timedCondition) {
		this.timedCondition = timedCondition;
	}

	
}
