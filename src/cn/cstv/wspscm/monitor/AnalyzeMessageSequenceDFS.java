package cn.cstv.wspscm.monitor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.cstv.wspscm.IImageKeys;

public class AnalyzeMessageSequenceDFS {
	private static TimedAutomataSet timedAutomataSet = AnalyzeMessageSequence.timedAutomataSet;
	private static List<State> timedAutomataState = AnalyzeMessageSequence.timedAutomataState;
	private static List<TimedAutomataSet> timedAutomataSetList = AnalyzeMessageSequence.timedAutomataSetList;
	private static List<List<State>> timedAutomataStateList =AnalyzeMessageSequence.timedAutomataStateList;//billy���ö��� ����.�б��е��б�
	public static Integer kSteps = -1;
	private static ConcurrentLinkedQueue<Mission> queue = new ConcurrentLinkedQueue<Mission>();// �������
	private static ConcurrentLinkedQueue<Mission> kqueue = new ConcurrentLinkedQueue<Mission>();// �������
	private static Stack<Mission>Stack = new Stack<Mission>();
	private static Stack<Mission>reStack = new Stack<Mission>();
	private static HashMap<String,String> messageMap=AnalyzeMessageSequence.messageMap;
	private static ArrayList<MissionForRefactor> foundList = new ArrayList<MissionForRefactor>();// ���������K�������п�����ϵ���Ϣ��Ϊ�ع���
	private static ArrayList<MissionForRefactor> tmpFoundList = new ArrayList<MissionForRefactor>();
   
	public static final int INNER_MESSAGE=0,SEND_MESSAGE=1,RECEIVE_MESSAGE=2,NON_INNER_MESSAGE=10;//�ڲ���Ϣ��������Ϣ��������Ϣ��
	private static final String PlaceArray = null;
	private static long currentTime1;
	private static List<Message> messageLog = new ArrayList<Message>();
	
	
	
	private static boolean isDebug=true,isForever=true;//isForever�����Ƿ�ͣ����K����trueΪ��ͣ��,isDebug�����Ƿ�Ӳ������Ϣ���й��ع���
	private static LinkedList<Mission> errorSequenceList=new LinkedList<Mission>();//��Ŵ�������е�����
	private static ArrayList<Long> timeList=new ArrayList<Long>();//���ÿ���ļ���ʱ��
    
	public static Integer getkSteps() {
		return kSteps;
	}
	public static void setkSteps(Integer kSteps) {
		
		if (kSteps > 0) {}
		else {System.out.println("Lookahead is closed.");}
		AnalyzeMessageSequenceDFS.kSteps = kSteps;	
		
		if(isForever){			// billy :��ͣ����K��
			new Thread(){   //ΪʲôҪ���¿���һ���̣߳���
				ArrayList<String> messageSeq=new ArrayList<String>();				
				public void run() {
					AnalyzeMessageSequence.setMyMessageSeq(messageSeq);//����messageSeq�������debug��ʱ��������Ӳ�����
					for(int j=0;j<1;j++){//ѭ��100��    ??billy
						synchronizedFindDFS();//tjf 20100925 ʹ����lookahead֮����Զ�����Ѱ�ң����پ���ÿ��10���messageview�̵߳���
			    	for(int i=0;i<messageSeq.size();i++){							
							try {
							Thread.currentThread().sleep(200);//�ȴ�2�����¿�ʼ��һ��ѭ��
						} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}			
						   AyalyzingPreLookAheadForRefactorDFS(messageSeq.get(i));
						}     
			    	   
						System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						System.out.println("Look ahead " + getkSteps() + " steps ��billy DFS�� " );
						System.out.println("The time list: ");
						System.out.println("the first: "+timeList.get(0));
						for(int i=0;i<messageSeq.size();i++){
							System.out.print(messageSeq.get(i)+": "+timeList.get(i+1)+"  ");
						}
						System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					}
				}
			}.start();
			
		}else
			synchronizedFindDFS();
		
			}

	static void synchronizedFindDFS(){
		if (kSteps == -1) {
			return;
		}
		
		foundList.clear();//�������ع������������������
		tmpFoundList.clear();
		errorSequenceList.clear();//�����ڴ�ӡ������ϵ��������
		timeList.clear();//���ʱ������
		
		if (timedAutomataStateList.size() == 0) {
			if (IImageKeys.automataFilesName.size() > 0) {
				long currentTime1 = System.nanoTime();

				AnalyzeMessageSequence.InitilizeAutomataStateWithMultiAutomata(IImageKeys.automataFilesName);

				System.out.println();


			}			
		}

		currentTime1 = System.nanoTime();

		if (kSteps > 0) {
			System.out.println();
			System.out.println("Look ahead " + kSteps + " steps ��billy DFS�� " );
			System.out.println();

		}

		
		AyalyzingPreLookAheadWithMultiAutomataDFS();		
		
	}
	private static void AyalyzingPreLookAheadWithMultiAutomataDFS() {
		if (queue.isEmpty()) {
		
			for (int i = 0; i < timedAutomataStateList.size(); i++) {// ��ÿ���Զ�������ʼ״̬��ʼ�ң�billy����ߵ�size ��ʾ�м����Զ�����
				ArrayList<String> placeArray = new ArrayList<String>(timedAutomataStateList.size());
				AnalyzeMessageSequence.initPlaceArray(placeArray);
				List<State> timedAutomataState = timedAutomataStateList.get(i);//billy���õ���i���Զ��� ���������״̬
				String stateName =placeArray.get(i); //billy����i���Զ����� ����
				for (int j = 0; j < timedAutomataState.size(); j++) {// ������ʼ״̬�����ֲ���timedAutomataState ��billy��ָ��i���Զ�����ÿ��״̬��
					State currentState = timedAutomataState.get(j);
					if (currentState.getStateName().equals(stateName)) {// �ҵ��˶�Ӧ���ֵ�state 
						List<String> innerList = currentState.getInnerMessageList();
						for (int k = 0; k < innerList.size(); k++) {// �ڲ���Ϣ���ǿ��ܵ�
							ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
							tmpPlaceArray.set(i, currentState.getEndStateName(innerList.get(k)));//billy�����ڲ���Ϣ��Ӧ��״̬  staname���뵽tmp
							queue.add(new Mission(innerList.get(k), (ArrayList<String>) tmpPlaceArray.clone(), 1));	
						}
						//System.out.println("queue whether empty");
						List<String> sendList = currentState.getSendMessageList();				
						for (int k = 0; k < sendList.size(); k++) {// ���ͱ���ͽ�����ԣ����ǿ��ܵ�
							ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
							int temp = AnalyzeMessageSequence.findInAllAuto(sendList.get(k),tmpPlaceArray);
							if (temp != -1) {
								tmpPlaceArray.set(i, currentState.getEndStateName(sendList.get(k)));
//								printPlaceArray(tmpPlaceArray);
								queue.add(new Mission(sendList
										.get(k),
										(ArrayList<String>) tmpPlaceArray.clone(),
										1));
								
							}
						}
				//		System.out.println("55 " + queue.size());
						break;//�ҵ��˵�һ����ʼ״̬�����������ٽ�����ѭ��
					}
				}
			}
		}
		System.out.println("\n Possible right message sequences:\n");
		while (!queue.isEmpty()) {
				Mission m = queue.poll();// �Ӷ���ȡ����ɾ
				
				
				if(m.getSteps()<kSteps){
					Stack.push(m);
		       
		        	 while(!Stack.isEmpty()){
		        		  Mission sm=Stack.pop();
		        		  String buffer = sm.getPreviousMessage();		  
		        		  ArrayList<String> placeArray = (ArrayList<String>) sm.getPlaceList().clone();
		        		  boolean isFind=false;
		        	 for (int i = 0; i < timedAutomataStateList.size(); i++) {
		    		List<State> timedAutomataState = timedAutomataStateList.get(i);
		    		
					for (int j = 0; j < timedAutomataState.size(); j++) {// ������һ״̬�����ֲ���timedAutomataState 
						State currentState = timedAutomataState.get(j);
					//	System.out.println("88 "+currentState.getStateName());
					//	AnalyzeMessageSequence.printPlaceArray(placeArray);	
						if (currentState.getStateName().equals(placeArray.get(i))) {// ���Զ���i���ҵ��˶�Ӧ���ֵ�state
							List<String> innerList = currentState.getInnerMessageList();
						//	System.out.println("99 " + innerList.size());
							for (int k = 0; k < innerList.size(); k++){// �ڲ���Ϣ���ǿ��ܵ�
								ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
								tmpPlaceArray.set(i, currentState.getEndStateName(innerList.get(k)));
//								printPlaceArray(tmpPlaceArray);
						       if(sm.getSteps()+1==kSteps)
						       {
						    	   kqueue.add(new Mission(buffer+" , "+innerList.get(k), (ArrayList<String>)tmpPlaceArray.clone(), sm.getSteps() + 1));
						    	  Mission km=kqueue.poll();
						    	  foundList.add(new MissionForRefactor(km.getPreviousMessage(),km.getPlaceList()));
						    	  System.out.println("---" + km.getPreviousMessage() + "---");
						    	  isFind=true;
						    	   
						       }else {
								//tmpPlaceArray.add("visited");
								Stack.push(new Mission(buffer+" , "+innerList.get(k), (ArrayList<String>)tmpPlaceArray.clone(), sm.getSteps() + 1));
								isFind=true;}
								}
							
							List<String> sendList = currentState.getSendMessageList();
//						
					for (int k = 0; k < sendList.size(); k++){// ���ͱ���ͽ�����ԣ����ǿ��ܵ�
								ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
								if (AnalyzeMessageSequence.findInAllAuto(sendList.get(k),tmpPlaceArray)!=-1){
									tmpPlaceArray.set(i, currentState.getEndStateName(sendList.get(k)));
							     if(sm.getSteps()+1==kSteps){
							    	 kqueue.add(new Mission(buffer+" , "+sendList.get(k), (ArrayList<String>)tmpPlaceArray.clone(), sm.getSteps() + 1));
							    	 Mission km =kqueue.poll();
							    	 foundList.add(new MissionForRefactor(km.getPreviousMessage(),km.getPlaceList()));
							    	 System.out.println("---" + km.getPreviousMessage() + "---");
							    	 isFind=true;
			
							     }else{Stack.push(new Mission(buffer+" , "+sendList.get(k), (ArrayList<String>)tmpPlaceArray.clone(), sm.getSteps() + 1));
							       	isFind=true;}
										}
								} 
								} //end current if	
						  
						}
		        	 }   //end out for //end inner for	
					 if(!isFind){  //
						boolean isMissionExist=false;
						for(Mission mission:errorSequenceList ){//�ж�һ��errorSequenceList��ԭ���ǲ����Ѿ������������
							if(mission.getPreviousMessage().equals(sm.getPreviousMessage())) {
								isMissionExist=true;
								break;								
							}
						}
						if((!isMissionExist)
//								&&(!isMissionLeave)&&(!flag)
								) errorSequenceList.add(sm);//���missionû�������ˣ����������������������д�ӡ
			//			for(int j=0;j<errorSequenceList.size();j++){
			//				Mission mission=errorSequenceList.get(j);
			//				System.out.println("errorsequecne"+mission.getPreviousMessage());}
				}
					
					   
						}//end empty
					
		        } //end if k
				else{ System.out.println("---" + m.getPreviousMessage() + "---��billy��");}
				}//end queue 
		
		for(MissionForRefactor mr:foundList){
			for(int i=0;i<errorSequenceList.size();i++){
				Mission mission=errorSequenceList.get(i);
				if(mr.getMessageSequence().contains(mission.getPreviousMessage())) 
					errorSequenceList.remove(mission); //�״γ��ֵ�mission
			}
		}
	
		//��ӡ���д�������
		System.out.println("\n Possible error message sequences��billy��:\n");
		for(int i=0;i<errorSequenceList.size();i++){
			Mission m=errorSequenceList.get(i);
			System.out.print("Warning: "+m.getPreviousMessage()+"\t");
			ArrayList<String> list=m.getPlaceList();
			 
               System.out.print("( ");
			for(int k=0;k<list.size();k++){
				System.out.print(list.get(k)+"("+IImageKeys.automataFilesName.get(k).
						substring(IImageKeys.automataFilesName.get(k).indexOf('/')+1,IImageKeys.automataFilesName.get(k).lastIndexOf('t'))+") ");
			}
			System.out.println(")");
		}
		if (kSteps > 0) {
			// System.out.println("Look ahead " + kSteps + " steps " +
			// (withControllability!=0?"with":"without") +
			// " controllability by " +
			// (isDepthSearchFirst!=0?"breadth search first.":"depth search first."));
			long current2=System.nanoTime();
			timeList.add((current2 - currentTime1) / 1000);
			System.out
					.println("\nThe execution time of initial Lookahead is:  "
							+ (current2 - currentTime1) / 1000 + " us");
		} else {
			System.out
					.println("The execution time of analysis without Pre-Lookahead is: "
							+ (System.nanoTime() - currentTime1) / 1000 + " us");

		}
		System.out
				.println("--------------���---------------���Զ����������-------------------------------------------");	
	
	}	//end ayalyzingpre dfs
	



      // �ع�˼��
public static void AyalyzingPreLookAheadForRefactorDFS(String messageName){
	if(messageName==null){
		System.out.println("No input message to drive the automachine!");
		return;	
	}
	
	LinkedList<Mission> errorSequenceList2=new LinkedList<Mission>();
	
	if (kSteps > 0) {
		System.out.println();
		System.out.println("Look ahead "
				+ kSteps
				+ " steps with message: "+messageMap.get(messageName)+"\nwhich is mapped into "+messageName+"billy refactor"
				);
		System.out.println();

	}
	
	currentTime1 = System.nanoTime();
	
	if(tmpFoundList.size()!=0){
		foundList=(ArrayList<MissionForRefactor>)tmpFoundList.clone();
		tmpFoundList.clear();
	}
	
	System.out.println("\n Possible right message sequences(billy refactor):\n");
	
	for (int i = 0; i < timedAutomataStateList.size(); i++) {// ��ÿ���Զ�������ʼ״̬��ʼ��
		ArrayList<String> placeArray ;		
		
		MissionForRefactor mfr;
		for(int index=0;index<foundList.size();index++)
		{
			mfr=foundList.get(index);				
			
			if(!mfr.getFirstMessageName().equals(messageName)) continue;//��������Ϣ��ԭ�����ҵ��������ﲻ���ʣ���������ѭ��
			
			placeArray=mfr.getPlaceArray();
			String tmpMessageSequence=mfr.getMessageSequenceWithoutFirstMessageName();
			boolean isFind=false;
		List<State> timedAutomataState = timedAutomataStateList.get(i);
//		String stateName = timedAutomataState.get(0).getStateName();
		String stateName =placeArray.get(i);
//		System.out.println("11 " + stateName);
		for (int j = 0; j < timedAutomataState.size(); j++) {// ������ʼ״̬�����ֲ���timedAutomataState
			State currentState = timedAutomataState.get(j);
//			System.out.println("22 ");
			if (currentState.getStateName().equals(stateName)) {// �ҵ��˶�Ӧ���ֵ�state
				List<String> innerList = currentState.getInnerMessageList();
//				System.out.println("33 " + innerList.size());
				for (int k = 0; k < innerList.size(); k++) {// �ڲ���Ϣ���ǿ��ܵ�
					ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
					tmpPlaceArray.set(i, currentState.getEndStateName(innerList.get(k)));
//					StringBuffer buffer=new StringBuffer(innerList.get(k));
//					synchronizedMoveInFindingMessageSeq(buffer,1,innerList.get(k),INNER_MESSAGE,tmpPlaceArray,i);
//					printPlaceArray(tmpPlaceArray);							
//					queue.add(new Mission(innerList.get(k), (ArrayList<String>) tmpPlaceArray.clone(), 1));	
					if(tmpMessageSequence.equals(""))
						tmpFoundList.add(new MissionForRefactor(innerList.get(k),tmpPlaceArray));
					else
						tmpFoundList.add(new MissionForRefactor(tmpMessageSequence+" , "+innerList.get(k),tmpPlaceArray));
					System.out.println("*** "+tmpMessageSequence+" , "+innerList.get(k)+" ***");
					isFind=true;
				}
				
				List<String> sendList = currentState.getSendMessageList();
				// List<String>
				// receiveList=currentState.getReceiveMessageList();
//				System.out.println("44 " + sendList.size());
				for (int k = 0; k < sendList.size(); k++) {// ���ͱ���ͽ�����ԣ����ǿ��ܵ�
					ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
					int temp =AnalyzeMessageSequence.findInAllAuto(sendList.get(k),tmpPlaceArray);
					if (temp != -1) {
						tmpPlaceArray.set(i, currentState.getEndStateName(sendList.get(k)));
//						printPlaceArray(tmpPlaceArray);
//						queue.add(new Mission(sendList.get(k),
//								(ArrayList<String>) tmpPlaceArray.clone(),
//								1));
						if(tmpMessageSequence.equals(""))
							tmpFoundList.add(new MissionForRefactor(sendList.get(k),tmpPlaceArray));
						else
							tmpFoundList.add(new MissionForRefactor(tmpMessageSequence+" , "+sendList.get(k),tmpPlaceArray));
						System.out.println("*** "+tmpMessageSequence+" , "+sendList.get(k)+" ***");
						isFind=true;
					}
				}
//				System.out.println("55 " + queue.size());
				break;//�ҵ��˵�һ����ʼ״̬�����������ٽ�����ѭ��
			}
		}
		if(!isFind) {
				boolean isMissionExist=false;
				for(Mission mission:errorSequenceList2 ){//�ж�һ��errorSequenceList��ԭ���ǲ����Ѿ������������
					if(mission.getPreviousMessage().equals(mfr.getMessageSequence())) {
						isMissionExist=true;
						break;								
					}
				}
				
				if(!isMissionExist)
					errorSequenceList2.add(new Mission(mfr.getMessageSequence(),mfr.getPlaceArray(),kSteps));//���missionû�������ˣ����������������������д�ӡ			
			
		}
	}
	}
	
	//�ٴ��ж��Ƿ�������������ȷ��ϵĲ��֣�����ȥ��
	for(MissionForRefactor mr:foundList){
		for(int i=0;i<errorSequenceList2.size();i++){
			Mission mission=errorSequenceList2.get(i);
			if(mr.getMessageSequence().contains(mission.getPreviousMessage())) 
				errorSequenceList2.remove(mission);
		}
	}
	
	System.out.println("\n Possible error message sequences:\n");
	for(int index=0;index<errorSequenceList2.size();index++){
		MissionForRefactor mfr=errorSequenceList2.get(index).toReconstrcut();
		if(!mfr.getFirstMessageName().equals(messageName)) continue;//��������Ϣ��ԭ�����ҵ��������ﲻ���ʣ���������ѭ��
		ArrayList<String> placeArray =mfr.getPlaceArray();
		String tmpMessageSequence=mfr.getMessageSequenceWithoutFirstMessageName();
		System.out.print("Warning: "+tmpMessageSequence+"\t");
		
		System.out.print("( ");
		for(int k=0;k<placeArray.size();k++){
			System.out.print(placeArray.get(k)+"("+IImageKeys.automataFilesName.get(k).
					substring(IImageKeys.automataFilesName.get(k).indexOf('/')+1,IImageKeys.automataFilesName.get(k).lastIndexOf('.'))+") ");				
		}
		System.out.println(")");
	}
	
	
	boolean isFind=true;
	while(isFind){
		isFind=false;
		for(int i=0;i<errorSequenceList.size();i++){
			Mission mission=errorSequenceList.get(i);
			if(!mission.getFirstMessageName().equals(messageName)) {					
				continue;
			}
			isFind=true;
			System.out.println("Warning: "+mission.getMessageSequenceWithoutFirstMessageName()+"\t");
			
			mission.setPreviousMessage(mission.getMessageSequenceWithoutFirstMessageName());
		
			ArrayList<String> placeArray =mission.getPlaceList();
			System.out.print("( ");
			for(int k=0;k<placeArray.size();k++){
				System.out.print(placeArray.get(k)+"("+IImageKeys.automataFilesName.get(k).
					substring(IImageKeys.automataFilesName.get(k).indexOf('/')+1,IImageKeys.automataFilesName.get(k).lastIndexOf('.'))+") ");				
			}
			System.out.println(")");
			}
	}
	
	long current2=System.nanoTime();
	timeList.add((current2 - currentTime1) / 1000);
	System.out.println("\n The execution time of analysis with reconstruct is: "
			+ (current2 - currentTime1) / 1000 + " us");
	System.out.println("*****************************************************************************");
}

 
} 

