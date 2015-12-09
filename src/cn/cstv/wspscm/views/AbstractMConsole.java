package cn.cstv.wspscm.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class AbstractMConsole implements MConsole {
	 private MessageConsole console;
	    
	    private MessageConsoleStream stream= null;
	    
	    public AbstractMConsole(MessageConsole console){
	        this.console = console;
	        this.stream = console.newMessageStream();
	    }
	    
	    /**
	     * ���MessageConsoleӦ�ñ��Ⱪ¶
	     */
	    public MessageConsole getMessageConsole(){
	        return this.console;
	    }
	    /**
	     * �����println�кܴ�ķ��ӿռ�
	     */
	    public void println(String msg){
	        StringBuffer sb = new StringBuffer();
	        sb.append(new SimpleDateFormat("[HH:mm:ss]").format(new Date()));
	        sb.append(msg);
	        this.stream.println(sb.toString());
	    }
}
