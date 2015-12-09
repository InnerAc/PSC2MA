package cn.cstv.wspscm.monitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.cstv.wspscm.IImageKeys;

public class AnalyzeMessageSequence {

	public static List<Boolean> sequentialObservation = new ArrayList<Boolean>();

	// private static List<Message> messageLog = new ArrayList<Message>();
	public static TimedAutomataSet timedAutomataSet = new TimedAutomataSet();
	static List<State> timedAutomataState = new ArrayList<State>();
	// public static Integer kSteps = -1;
	// public static Integer withControllability = 0; //0����Error/Accept,
	// 1����Controllability/Uncontrollability
	// public static Integer isDepthSearchFirst = 0; //0����������������1���������������
	private static State currentProcessState = null;
	private static State nextProcessState = null;
	public static List<List<State>> timedAutomataStateListPSC = new ArrayList<List<State>>();

	private static List<State> acceptState = new ArrayList<State>();
	private static List<State> errorState = new ArrayList<State>();

	private static List<String> errorMessage = new ArrayList<String>();
	private static List<Integer> acceptMessage = new ArrayList<Integer>();

	// public static List<List<Boolean>> sequentialObservationList = new
	// ArrayList<List<Boolean>>();

	private static List<Message> messageLog = new ArrayList<Message>();
	public static List<TimedAutomataSet> timedAutomataSetList = new ArrayList<TimedAutomataSet>();
	public static List<List<State>> timedAutomataStateList = new ArrayList<List<State>>();//billy���ö��� ����.�б��е��б�
	public static Integer kSteps = -1;
	public static Integer withControllability = 0; // 0����Error/Accept,
													// 1����Controllability/Uncontrollability
	public static Integer isDepthSearchFirst = 0; // 0����������������1���������������
	private static List<State> currentProcessStateList = new ArrayList<State>();
	private static List<State> nextProcessStateList = new ArrayList<State>();

	private static List<List<State>> acceptStateList = new ArrayList<List<State>>();
	private static List<List<State>> errorStateList = new ArrayList<List<State>>();

	private static List<List<String>> errorMessageList = new ArrayList<List<String>>();
	private static List<List<Integer>> acceptMessageList = new ArrayList<List<Integer>>();

	private static ArrayList<MissionForRefactor> foundList = new ArrayList<MissionForRefactor>();// ���������K�������п�����ϵ���Ϣ��Ϊ�ع���
	private static ArrayList<MissionForRefactor> tmpFoundList = new ArrayList<MissionForRefactor>();
	private static ConcurrentLinkedQueue<Mission> queue = new ConcurrentLinkedQueue<Mission>();// �������
	public static HashMap<String,String> messageMap=new HashMap<String,String>();
//	private static String tempName = null;
//	private static boolean flag=false,flag2=false; 
	private static long currentTime1;
	
	public static final int INNER_MESSAGE=0,SEND_MESSAGE=1,RECEIVE_MESSAGE=2,NON_INNER_MESSAGE=10;//�ڲ���Ϣ��������Ϣ��������Ϣ��
																	//���ڲ���Ϣ����Ҫô���ͣ�Ҫô���ա����ǵ�������Ϣ���ͣ�ֻ��Ϊ�˱��������ڲ���Ϣ�� 
	public static final int ERROR_MESSAGE=-1,ABNORMAL_MESSAGE=-2;
	
	private static boolean isDebug=true,isForever=true;//isForever�����Ƿ�ͣ����K����trueΪ��ͣ��,isDebug�����Ƿ�Ӳ������Ϣ���й��ع���
	private static LinkedList<Mission> errorSequenceList=new LinkedList<Mission>();//��Ŵ�������е�����
	private static ArrayList<Long> timeList=new ArrayList<Long>();//���ÿ���ļ���ʱ��

	public static Integer getkSteps() {
		return kSteps;
	}

	public static void setkSteps(Integer kSteps) {
		if (kSteps > 0) {
//			System.out.println("Look ahead "
//					+ kSteps
//					+ " steps "
//					+ (withControllability != 0 ? "with" : "without")
//					+ " controllability by "
//					+ (isDepthSearchFirst != 0 ? "breadth search first."
//							: "depth search first."));
		} else {
			System.out.println("Lookahead is closed.");
		}
		AnalyzeMessageSequence.kSteps = kSteps;			
		
//		if(isDebug) return;
		
		if(isForever){			// billy :��ͣ����K��
			new Thread(){   //ΪʲôҪ���¿���һ���̣߳���
				ArrayList<String> messageSeq=new ArrayList<String>();				
				public void run() {
					setMyMessageSeq(messageSeq);//����messageSeq�������debug��ʱ��������Ӳ�����
					for(int j=0;j<1;j++){//ѭ��100��    ??billy
						synchronizedFind();//tjf 20100925 ʹ����lookahead֮����Զ�����Ѱ�ң����پ���ÿ��10���messageview�̵߳���
					for(int i=0;i<messageSeq.size();i++){							
							try {
								Thread.currentThread().sleep(200);//�ȴ�2�����¿�ʼ��һ��ѭ��
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}			
							AyalyzingPreLookAheadForRefactor(messageSeq.get(i));
						}
						System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						System.out.println("Look ahead " + getkSteps() + " steps ��billy BFS�� " );
						System.out.println("The time list: ");
						System.out.println("the first: "+timeList.get(0));
						for(int i=0;i<messageSeq.size();i++){
							System.out.print(messageSeq.get(i)+": "+timeList.get(i+1)+"  ");
						}
						System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					}
				}
			}.start();
			
		}else
			synchronizedFind();
		
			
	}
	
	private static void synchronizedFind(){
		if (kSteps == -1) {
			return;
		}
		
		foundList.clear();//�������ع������������������
		tmpFoundList.clear();
		errorSequenceList.clear();//�����ڴ�ӡ������ϵ��������
		timeList.clear();//���ʱ������
		
		// if (timedAutomataStateList.size() == 0 &&
		// currentProcessStateList.size() == 0) {//tjf
		if (timedAutomataStateList.size() == 0) {
			if (IImageKeys.automataFilesName.size() > 0) {
				long currentTime1 = System.nanoTime();

				InitilizeAutomataStateWithMultiAutomata(IImageKeys.automataFilesName);

				System.out.println();
//				System.out
//						.println("The execution time of InitilizeAutomataState is: "
//								+ (System.nanoTime() - currentTime1)
//								/ 1000
//								+ " us");
//				System.out
//						.println("------------------------------------------------------------------------");

			}			
		}

		currentTime1 = System.nanoTime();

		if (kSteps > 0) {
			System.out.println();
			System.out.println("Look ahead "
					+ kSteps
					+ " steps ��billy BFS�� "
					);
			System.out.println();

		}
//		GetMessageFromMessageSequence(messageString);
		// AnalyzingWithoutTimeRealTime();
		// AyalyzingWithoutTimePreLookAheadWithMultiAutomata();//tjf
		
		AyalyzingPreLookAheadWithMultiAutomata();		
		
	}
	
	
	private static void AyalyzingPreLookAheadWithMultiAutomata() {//����д��һ����������ɿ������е�Ѱ�ҹ���
//		ArrayList<String> messageSeq=new ArrayList<String>();
//		setMyMessageSeq(messageSeq);//����messageSeq�������debug��ʱ��������Ӳ�����

		if (queue.isEmpty()) {// ��һ�Σ������ǿյ�
//			System.out.println("queue is empty " + initStateNameList.size());
			// State s=timedAutomataState.get(0);
			for (int i = 0; i < timedAutomataStateList.size(); i++) {// ��ÿ���Զ�������ʼ״̬��ʼ�ң�billy����ߵ�size ��ʾ�м����Զ�����
				ArrayList<String> placeArray = new ArrayList<String>(timedAutomataStateList.size());
				
//				if(messageSeq.size()==0) 
//					initPlaceArray(placeArray);//��messageSeq����Ϊ0������£���λ�������ʼ��Ϊ���Զ�������ʼ״̬
//				else{
//					initPlaceArray(placeArray);
////					printPlaceArray(placeArray);
//					initPlaceArrayForKSteps(placeArray,messageSeq);
////					printPlaceArray(placeArray);	
//				}
				initPlaceArray(placeArray);//��messageSeq����Ϊ0������£���λ�������ʼ��Ϊ���Զ�������ʼ״̬    
				
				List<State> timedAutomataState = timedAutomataStateList.get(i);//billy���õ���i���Զ��� ���������״̬
//				String stateName = timedAutomataState.get(0).getStateName();
				String stateName =placeArray.get(i); //billy����i���Զ����� ���� 
			//	System.out.println("11 " + stateName);
				for (int j = 0; j < timedAutomataState.size(); j++) {// ������ʼ״̬�����ֲ���timedAutomataState ��billy��ָ��i���Զ�����ÿ��״̬��
					State currentState = timedAutomataState.get(j);
			//		System.out.println("22 ");
					if (currentState.getStateName().equals(stateName)) {// �ҵ��˶�Ӧ���ֵ�state 
						List<String> innerList = currentState.getInnerMessageList();
				//		System.out.println("33 " + innerList.size());
						for (int k = 0; k < innerList.size(); k++) {// �ڲ���Ϣ���ǿ��ܵ�
							ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
							tmpPlaceArray.set(i, currentState.getEndStateName(innerList.get(k)));//billy�����ڲ���Ϣ��Ӧ��״̬  staname���뵽tmp
//							StringBuffer buffer=new StringBuffer(innerList.get(k));
//							synchronizedMoveInFindingMessageSeq(buffer,1,innerList.get(k),INNER_MESSAGE,tmpPlaceArray,i);
	//						printPlaceArray(tmpPlaceArray);						
							queue.add(new Mission(innerList.get(k), (ArrayList<String>) tmpPlaceArray.clone(), 1));
					//		System.out.println("�ڲ���Ϣ��tmpPlaceArray��ֵ"+tmpPlaceArray);
						}
						List<String> sendList = currentState.getSendMessageList();				
				//		List<String> receiveList=currentState.getReceiveMessageList();
					//	System.out.println("44 " + sendList.size());
						for (int k = 0; k < sendList.size(); k++) {// ���ͱ���ͽ�����ԣ����ǿ��ܵ�
							ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
							int temp = findInAllAuto(sendList.get(k),tmpPlaceArray);
							if (temp != -1) {
								tmpPlaceArray.set(i, currentState.getEndStateName(sendList.get(k)));
//								printPlaceArray(tmpPlaceArray);
								queue.add(new Mission(sendList
										.get(k),
										(ArrayList<String>) tmpPlaceArray.clone(),
										1));
								
							}
						}
				//		System.out.println("�ڲ���Ϣ��tmpPlaceArray��ֵ"+tmpPlaceArray);
				//		System.out.println("55 " + queue.size());
						break;//�ҵ��˵�һ����ʼ״̬�����������ٽ�����ѭ��
					}
				}
			}
		}
	//	System.out.println("��ʼ��k��֮ǰ�Ĵ���"+queue.size());
		System.out.println("\n Possible right message sequences:\n");
		
			while (!queue.isEmpty()) {
			//	System.out.println("66 " + queue.size());
				Mission m = queue.poll();// �Ӷ���ȡ����ɾ
				boolean isFind=false;
				if (m.getSteps() < kSteps) {// ��û��K�����������ҿ��ܵ����
					String buffer = m.getPreviousMessage();
			//		System.out.println("--->>> " + m.getPreviousMessage() + " <<<---");
					// String nextName=m.getNextState1();
					// String nextName2=m.getNextState2();
					// int no1=m.getNo1();
					// int no2=m.getNo2();
					ArrayList<String> placeArray = m.getPlaceList();
					
			//		System.out.println("77 " + buffer+" steps: "+m.getSteps());
					for (int i = 0; i < timedAutomataStateList.size(); i++) {
						List<State> timedAutomataState = timedAutomataStateList.get(i);
						for (int j = 0; j < timedAutomataState.size(); j++) {// ������һ״̬�����ֲ���timedAutomataState 
							State currentState = timedAutomataState.get(j);
					//		System.out.println("88 "+currentState.getStateName());
							if (currentState.getStateName().equals(placeArray.get(i))) {// ���Զ���i���ҵ��˶�Ӧ���ֵ�state
								List<String> innerList = currentState.getInnerMessageList();
				//	System.out.println("99innerList.size()++ " + innerList.size());
								for (int k = 0; k < innerList.size(); k++){// �ڲ���Ϣ���ǿ��ܵ�
									ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
									tmpPlaceArray.set(i, currentState.getEndStateName(innerList.get(k)));
//									printPlaceArray(tmpPlaceArray);
						//			System.out.println("ִ��innerList.size()++");
									queue.add(new Mission(buffer+" , "+innerList.get(k), (ArrayList<String>)tmpPlaceArray.clone(), m.getSteps() + 1));
//									StringBuffer tmpbuffer=new StringBuffer(innerList.get(k));
//									synchronizedMoveInFindingMessageSeq(tmpbuffer,m.getSteps(),innerList.get(k),INNER_MESSAGE,tmpPlaceArray,i);
									isFind=true;
								}
								List<String> sendList = currentState
										.getSendMessageList();
//								List<String> receiveList = currentState
//										.getReceiveMessageList();
//								System.out.println("991 " + sendList.size());
								for (int k = 0; k < sendList.size(); k++){// ���ͱ���ͽ�����ԣ����ǿ��ܵ�
									ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
//									printPlaceArray(tmpPlaceArray);
									if (findInAllAuto(sendList.get(k),tmpPlaceArray)!=-1){
										tmpPlaceArray.set(i, currentState.getEndStateName(sendList.get(k)));
//										printPlaceArray(tmpPlaceArray);
										queue.add(new Mission(buffer+" , "+sendList.get(k),(ArrayList<String>)tmpPlaceArray.clone(), m.getSteps() + 1));
						//				System.out.println("��ӡtmpֵ"+tmpPlaceArray);
										isFind=true;
									}}								
							//	System.out.println("00 break���� " + queue.size());
								break;//�ҵ��ˣ��Ͳ���ѭ���ˣ���Ϊͬһ��״̬û�еڶ����洢λ��
							}
						}}
						if(!isFind){  //
							boolean isMissionExist=false;
							for(Mission mission:errorSequenceList ){//�ж�һ��errorSequenceList��ԭ���ǲ����Ѿ������������
								if(mission.getPreviousMessage().equals(m.getPreviousMessage())) {
									isMissionExist=true;
									break;								
								}
							}
//							boolean isMissionLeave=false;
//							for(Mission mission:queue){//��ǰ���еķ�֧��û���ж��꣬���ܻ�������ȷ������������Ļ����ñ�־λ��ȷ��������ӵ�errorSequenceList
////								System.out.println("Ml "+mission.getPreviousMessage());
////								System.out.println("m2 "+m.getPreviousMessage());
//								if(mission.getPreviousMessage().contains(m.getPreviousMessage())) {
//									System.out.println("m2 "+m.getPreviousMessage());
//									isMissionLeave=true;
//									break;								
//								}
//							}
//							boolean flag=false;
//							for(MissionForRefactor mr:foundList){//ȷ����ǰ��ȷ��������û����������Ϣ
////								System.out.println("YY "+mr.getMessageSequence());
//								if(mr.getMessageSequence().contains(m.getPreviousMessage())){
//									flag=true;
//									break;
//								}
//							}
							if((!isMissionExist)
//									&&(!isMissionLeave)&&(!flag)
									) errorSequenceList.add(m);//���missionû�������ˣ����������������������д�ӡ
			//				for(int j=0;j<errorSequenceList.size();j++){
			//					Mission mission=errorSequenceList.get(j);
			//					System.out.println("errorsequecne"+mission.getPreviousMessage());}
						}
					
					// queue.remove(m);//��m�Ӷ�����ɾ��
				} else if (m.getSteps() == kSteps) {// �Ѿ�����ָ����k���Ѷ��������е�mission���ݴ��������					
					System.out.println("---" + m.getPreviousMessage() + "---");
					foundList.add(new MissionForRefactor(m.getPreviousMessage(),m.getPlaceList()));
					// queue.remove(m);//��m�Ӷ�����ɾ��
//					flag=true;//ʹ�����ֻ��ӡһ��
				}
			}
			

			//�ٴ��ж��Ƿ�������������ȷ��ϵĲ��֣�����ȥ��   ��
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
			
//			System.out.println();
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
					.println("-----------------------------���Զ����������-------------------------------------------");	
		
	}
	
	/**רΪ�ع���Ƶ�һ������
	 * */
	public static void AyalyzingPreLookAheadForRefactor(String messageName){
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
//			String stateName = timedAutomataState.get(0).getStateName();
			String stateName =placeArray.get(i);
//			System.out.println("11 " + stateName);
			for (int j = 0; j < timedAutomataState.size(); j++) {// ������ʼ״̬�����ֲ���timedAutomataState
				State currentState = timedAutomataState.get(j);
//				System.out.println("22 ");
				if (currentState.getStateName().equals(stateName)) {// �ҵ��˶�Ӧ���ֵ�state
					List<String> innerList = currentState.getInnerMessageList();
//					System.out.println("33 " + innerList.size());
					for (int k = 0; k < innerList.size(); k++) {// �ڲ���Ϣ���ǿ��ܵ�
						ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
						tmpPlaceArray.set(i, currentState.getEndStateName(innerList.get(k)));
//						StringBuffer buffer=new StringBuffer(innerList.get(k));
//						synchronizedMoveInFindingMessageSeq(buffer,1,innerList.get(k),INNER_MESSAGE,tmpPlaceArray,i);
//						printPlaceArray(tmpPlaceArray);							
//						queue.add(new Mission(innerList.get(k), (ArrayList<String>) tmpPlaceArray.clone(), 1));	
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
//					System.out.println("44 " + sendList.size());
					for (int k = 0; k < sendList.size(); k++) {// ���ͱ���ͽ�����ԣ����ǿ��ܵ�
						ArrayList<String> tmpPlaceArray=(ArrayList<String>)placeArray.clone();
						int temp = findInAllAuto(sendList.get(k),tmpPlaceArray);
						if (temp != -1) {
							tmpPlaceArray.set(i, currentState.getEndStateName(sendList.get(k)));
//							printPlaceArray(tmpPlaceArray);
//							queue.add(new Mission(sendList.get(k),
//									(ArrayList<String>) tmpPlaceArray.clone(),
//									1));
							if(tmpMessageSequence.equals(""))
								tmpFoundList.add(new MissionForRefactor(sendList.get(k),tmpPlaceArray));
							else
								tmpFoundList.add(new MissionForRefactor(tmpMessageSequence+" , "+sendList.get(k),tmpPlaceArray));
							System.out.println("*** "+tmpMessageSequence+" , "+sendList.get(k)+" ***");
							isFind=true;
						}
					}
//					System.out.println("55 " + queue.size());
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
	
	/**a :λ������
	 * */
	static void initPlaceArray(ArrayList<String> a) {//��λ�������ʼ��Ϊ���Զ�������ʼ״̬
		for (int j = 0; j < timedAutomataStateList.size(); j++) {
			a.add(timedAutomataStateList.get(j).get(0).getStateName());//billy:�õ�ÿ���Զ���������
		//	printPlaceArray(a);
			
		}
	}
	
	/**a :λ������
	 * ����Ϣ����������ʱִ�У���K��ǰ��׼���������ƶ�λ������
	 * */
	private static void initPlaceArrayForKSteps(ArrayList<String> a,ArrayList<String> messageSeq){ // billy��û���õ���
		ConcurrentLinkedQueue<ExamineMission> examineQueue=new ConcurrentLinkedQueue<ExamineMission> ();
		String messageName=messageSeq.get(0);//��һ����Ϣ����ȡ��	
		int messageType=AnalyseMessageType(messageName);//�ж��������Ϣ�����ͣ��Լ��Ƿ�Ϸ�
			for (int i = 0; i < timedAutomataStateList.size(); i++) {
//				boolean isFind=false;
				ArrayList<String> placeArray = (ArrayList<String>)a.clone();
				List<State> timedAutomataState = timedAutomataStateList.get(i);
				String stateName = timedAutomataState.get(0).getStateName();
				for (int j = 0; j < timedAutomataState.size(); j++) {// ������ʼ״̬�����ֲ���timedAutomataState
					State currentState = timedAutomataState.get(j);

//					System.out.println("22 ");
					if (currentState.getStateName().equals(stateName)) {// �ҵ��˶�Ӧ��ʼ״̬��state
						switch(messageType){
						case INNER_MESSAGE:
							List<String> innerList = currentState.getInnerMessageList();
//							System.out.println("33 " + innerList.size());
							for (int k = 0; k < innerList.size(); k++) {// 
								if(innerList.get(k).equals(messageName)){
									ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
									leftMessage.remove(messageName);

									placeArray.set(i, currentState.getEndStateName(innerList.get(k)));
									synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//									printPlaceArray(placeArray);
									examineQueue.add(new ExamineMission(innerList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
									break;
								}
							}
							break;
						case NON_INNER_MESSAGE:
							List<String> sendList = currentState.getSendMessageList();
							// List<String>
							// receiveList=currentState.getReceiveMessageList();
//							System.out.println("44 " + sendList.size());
							for (int k = 0; k < sendList.size(); k++) {
								if(sendList.get(k).equals(messageName)){
									ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
									leftMessage.remove(messageName);
									placeArray.set(i, currentState.getEndStateName(sendList.get(k)));
									synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//									printPlaceArray(placeArray);
									examineQueue.add(new ExamineMission(sendList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
									break;
								}
							}
							List<String> receiveList=currentState.getReceiveMessageList();
							for (int k = 0; k < receiveList.size(); k++) {
								if(receiveList.get(k).equals(messageName)){
									ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
									leftMessage.remove(messageName);
									placeArray.set(i, currentState.getEndStateName(receiveList.get(k)));
									synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//									printPlaceArray(placeArray);
									examineQueue.add(new ExamineMission(receiveList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
									break;
								}
							}
							break;
						default:
								System.out.println("default");
						}
						
//						System.out.println("55 " + queue.size());
						break;//�ҵ��˵�һ����ʼ״̬�����������ٽ�����ѭ��
					}
				}
			}
			
		
	if(examineQueue.isEmpty()){//��һ����Ϣ��û��ƥ�䣬��Ȼ�˳�
		System.out.println("Don't find because of the first message name in message sequence! \n Therefore looking ahead k steps is cancelled!!!");		
		return;
	}
	
	while(!examineQueue.isEmpty()){
			ExamineMission m = examineQueue.poll();// �Ӷ���ȡ����ɾ
			messageSeq=m.getList();
//			System.out.println(examineQueue.size()+" ***");
			boolean isFind=false;
			if (messageSeq.size()!=0) {// ��û�ж���ϸ�������Ϣ����	
//				System.out.println(messageSeq.size());
				messageName=messageSeq.get(0);
				messageType=AnalyseMessageType(messageName);//�ж��������Ϣ�����ͣ��Լ��Ƿ�Ϸ�
				ArrayList<String> placeArray = m.getPlaceList();
				for (int i = 0; i < timedAutomataStateList.size(); i++) {
					List<State> timedAutomataState = timedAutomataStateList.get(i);
					for (int j = 0; j < timedAutomataState.size(); j++) {// ������һ״̬�����ֲ���timedAutomataState
						State currentState = timedAutomataState.get(j);
//						System.out.println("88 "+currentState.getStateName());
//						System.out.println("$$$ "+currentState.getStateName());
//						System.out.println("$$$$$ "+placeArray.get(i));
						if (currentState.getStateName().equals(placeArray.get(i))) {// ���Զ���i���ҵ��˶�Ӧ���ֵ�state
							switch(messageType){
							case INNER_MESSAGE:
								List<String> innerList = currentState.getInnerMessageList();
//								System.out.println("99 " + innerList.size());
								for (int k = 0; k < innerList.size(); k++){// 
//									System.out.println("&&& "+innerList.get(k));
									if(innerList.get(k).equals(messageName)){
										isFind=true;
										ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
										leftMessage.remove(messageName);
										placeArray.set(i, currentState.getEndStateName(innerList.get(k)));
										synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//										printPlaceArray(placeArray);
										examineQueue.add(new ExamineMission(innerList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
										break;
									}
								}
								break;
							case NON_INNER_MESSAGE:
								List<String> sendList = currentState.getSendMessageList();
								List<String> receiveList = currentState
										.getReceiveMessageList();
//								System.out.println("991 " + sendList.size());
								for (int k = 0; k < sendList.size(); k++){
//									System.out.println("%%% "+sendList.get(k));
									if(sendList.get(k).equals(messageName))
									{
										isFind=true;
										ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
										leftMessage.remove(messageName);
										placeArray.set(i, currentState.getEndStateName(sendList.get(k)));
										synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//										printPlaceArray(placeArray);
										examineQueue.add(new ExamineMission(sendList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
										break;
									}
								}
								for (int k = 0; k < receiveList.size(); k++){
//									System.out.println("%%%% "+receiveList.get(k));
									if(receiveList.get(k).equals(messageName))
									{
										isFind=true;
										ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
										leftMessage.remove(messageName);
										placeArray.set(i, currentState.getEndStateName(receiveList.get(k)));
										synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//										printPlaceArray(placeArray);
										examineQueue.add(new ExamineMission(receiveList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
										break;
									}
								}
								break;
							}																
//							System.out.println("00 " + queue.size());
							break;//�ҵ��ˣ��Ͳ���ѭ���ˣ���Ϊͬһ��״̬û�еڶ����洢λ��
						}
					}						
				}
				if(!isFind&&(examineQueue.size()==0)){//û���ҵ������Ҷ�����Ҳû�������ˣ�ƥ��ʧ�ܣ��˳�
					System.out.println("Not Find!");
					System.out.println("Therefore looking ahead k steps is cancelled!!!");
					return;
				}
				
			}
			else  {// ���жϵ���Ϣ���ж��ж����ˣ��ɹ�
				System.out.println("--- The Message Sequence has been found in automachine! ---");
				ArrayList<String> tmp=m.getPlaceList();
				for (int i=0;i<tmp.size();i++){
					a.set(i,tmp.get(i));
				}				
				return;
			}
		}
	
	}
	
	public static void printPlaceArray(ArrayList<String> a){
		for (int j = 0; j < a.size(); j++) {
			System.out.print(a.get(j)+" ");
		}
		System.out.println();
	}



	public static int findInAllAuto(String name, ArrayList<String> a) {//�ҵ��뷢����ԵĽ�����Ϣ���ڵ��Զ���
		// tempHashmap.clear();
//		String tempName = null;
		for (int i = 0; i < timedAutomataStateList.size(); i++) {
			List<State> timedAutomataState = timedAutomataStateList.get(i);
			for (int j = 0; j < timedAutomataState.size(); j++) {
				State s = timedAutomataState.get(j);
				if(s==null) System.out.println("s is null");
				if(a==null) System.out.println("a is null");
				if(a.get(i)==null) System.out.println("a.get i is null "+i+" "+a.size());
				if(s.getStateName()==null) System.out.println("s.getstatename is null");
				List<String> l;
				if (a.get(i).equals(s.getStateName())) { //billy������a.get(i)ȡ������״̬S0�� equal =s.getstatename  
					l = s.getReceiveMessageList();
					for(int k=0;k<l.size();k++){
					if (l.get(k).equals(name)) {						
						a.set(i, s.getEndStateName(name));//�����ҵ���Ӧ����������Զ���������λ������������
						return i;
				         
					}
					}
				}
			}
		}
		return -1;
	}
	
	public static void setMyMessageSeq(ArrayList<String> messageSeq){
		if(isDebug){ //billy �� �����ʾʹ��Ӳ���� Ϊ���ع�
			messageSeq.add("[receive]SubmitRequst");
			messageSeq.add("AgentBusy");
			messageSeq.add("AgentFree");
			messageSeq.add("[invoke]SendPatientCondition(patientCondition)");
			messageSeq.add("hospitalList");
			messageSeq.add("CompareHospital");
			messageSeq.add("EnsureHospital");
			messageSeq.add("[invoke]ChoosePrimaryHospital(hospitalList)");
			messageSeq.add("primaryHospital");
			messageSeq.add("makeAppointment(detailDate)");
			
			
			messageMap.put("[receive]SubmitRequst", "[MedicalServiceAgent]org.equinoxosgi.toast.internal.client.emergency.EmergencyMonitor.[receive]SubmitRequst");
			messageMap.put("AgentBusy","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.AgentBusy");
			messageMap.put("AgentFree","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.AgentFree");
			messageMap.put("[invoke]SendPatientCondition(patientCondition)","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.[invoke]SendPatientCondition(patientCondition)");
			messageMap.put("hospitalList","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.hospitalList");
			messageMap.put("CompareHospital","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.CompareHospital");
			messageMap.put("EnsureHospital","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.EnsureHospital");
			messageMap.put("[invoke]ChoosePrimaryHospital(hospitalList)","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.[invoke]ChoosePrimaryHospital(hospitalList)");
			messageMap.put("primaryHospital","[MedicalServiceAgent]org.equinoxosgi.toast.internal.dev.gps.sim.GpsCompassSensor.primaryHospital");
			messageMap.put("makeAppointment(detailDate)","[MedicalServiceAgent]org.equinoxosgi.toast.core.channel.sender.ChannelMessage.makeAppointment(detailDate)");
			
		}
		else
			for(int i=0;i<messageLog.size();i++){
				Message m=messageLog.get(i);
				messageSeq.add(m.getMessageText());
			}
	}
	
	/**������Ϣ������ĳ����Ϣ���ж����ǲ����ڲ���Ϣ
	 * */
	private static int AnalyseMessageType(String messageName){
		boolean findInSend=false,findInReceive=false;
		for (int i = 0; i < timedAutomataStateList.size(); i++) {
			List<State> timedAutomataState = timedAutomataStateList.get(i);
			for (int j = 0; j < timedAutomataState.size(); j++) {
				State currentState = timedAutomataState.get(j);
				List<String> innerList = currentState.getInnerMessageList();
				if(innerList.contains(messageName)){//�ڲ���Ϣ�����а�������Ϣ�������ڲ���Ϣ�ı�ţ�0
					return INNER_MESSAGE;
				}
				List<String> sendList = currentState.getSendMessageList();
				if(sendList.contains(messageName)){//������Ϣ�����а�������Ϣ����Ҫ������
					if(!findInSend) findInSend=true;//ԭ��û�з��ֹ������־λ
					if(findInReceive) return NON_INNER_MESSAGE;//ԭ�����ֹ������ط��ڲ���Ϣ�ı��
				}
				List<String> receiveList = currentState.getReceiveMessageList();
				if(receiveList.contains(messageName)){
					if(!findInReceive) findInReceive=true;
					if(findInSend) return NON_INNER_MESSAGE;
				}
			}
		}
		
		if(!(findInSend&&findInReceive))//���ڲ���Ե���������Զ�������������ʾ���û�
			if(findInSend||findInReceive) {
				System.out.println("The message name: "+messageName+" isn't matching! \n  Please check the auto file!!!");
				return ERROR_MESSAGE;
			}else{
				System.out.println("The message name: "+messageName+" can't be found! \n  Please check the auto file!!!");
				return ERROR_MESSAGE;
			}
		
		return ABNORMAL_MESSAGE;
	}
	

	
	/**messageName��ҪѰ�ҵ��Ǹ���Ϣ��
	 * messageType��ҪѰ�ҵ��Ǹ���Ϣ����
	 * list����ʾѰ��λ�õ�����
	 * except��ͬ��ʱ������Ǹ��Զ�������Ϊ��examineMessageSeq�Ѿ��ҵ���
	 * */
	private static void synchronizedMoveByMessageSeq(String messageName,int messageType,ArrayList<String> list,int except){
		for (int i = 0; i < timedAutomataStateList.size(); i++) {
			if(i==except) continue;
			List<State> timedAutomataState = timedAutomataStateList.get(i);
			String stateName=list.get(i);
			for (int j = 0; j < timedAutomataState.size(); j++) {
				State s = timedAutomataState.get(j);
				List<String> l;
				switch(messageType){
				case INNER_MESSAGE:
					if (stateName.equals(s.getStateName())) {
						l = s.getInnerMessageList();
						for(int k=0;k<l.size();k++){
						if (l.get(k).equals(messageName)) {
							list.set(i, s.getEndStateName(messageName));							
						}
						}
					}
					break;
					default:
						if (stateName.equals(s.getStateName())) {
							l=s.getSendMessageList();
							for(int k=0;k<l.size();k++){
								if (l.get(k).equals(messageName)) {
									list.set(i, s.getEndStateName(messageName));							
								}
							}
							l=s.getReceiveMessageList();
							for(int k=0;k<l.size();k++){
								if (l.get(k).equals(messageName)) {
									list.set(i, s.getEndStateName(messageName));							
								}
							}
						}
				}				
			}
		}
	}
	
	public static void examineMessageSeq(){		//����д�ĵڶ��������������жϴ������Ϣ�����Ƿ�Ϊ�Զ�����֧�ֵ�����
		ArrayList<String> messageSeq=new ArrayList<String>();
		setMyMessageSeq(messageSeq);//����messageSeq�������debug��ʱ��������Ӳ�����
		
		ConcurrentLinkedQueue<ExamineMission> examineQueue=new ConcurrentLinkedQueue<ExamineMission> ();
		if(messageSeq.size()==0) return;		
		
		if (timedAutomataStateList.size() == 0) {//ȷ�����е��Զ�����Ϣ����ȷ�ļ��ؽ�����
			if (IImageKeys.automataFilesName.size() > 0) {				
				InitilizeAutomataStateWithMultiAutomata(IImageKeys.automataFilesName);
			}			
		}
		
			long currentTime = System.nanoTime();//��¼�¿�ʼִ�е�ʱ��
			String messageName=messageSeq.get(0);//��һ����Ϣ����ȡ��	
			int messageType=AnalyseMessageType(messageName);//�ж��������Ϣ�����ͣ��Լ��Ƿ�Ϸ�
				for (int i = 0; i < timedAutomataStateList.size(); i++) {
//					boolean isFind=false;
					ArrayList<String> placeArray = new ArrayList<String>(timedAutomataStateList.size());
					initPlaceArray(placeArray);
					List<State> timedAutomataState = timedAutomataStateList.get(i);
					String stateName = timedAutomataState.get(0).getStateName();
					for (int j = 0; j < timedAutomataState.size(); j++) {// ������ʼ״̬�����ֲ���timedAutomataState
						State currentState = timedAutomataState.get(j);
//						System.out.println("22 ");
						if (currentState.getStateName().equals(stateName)) {// �ҵ��˶�Ӧ��ʼ״̬��state
							switch(messageType){
							case INNER_MESSAGE:
								List<String> innerList = currentState.getInnerMessageList();
//								System.out.println("33 " + innerList.size());
								for (int k = 0; k < innerList.size(); k++) {// 
									if(innerList.get(k).equals(messageName)){
										ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
										leftMessage.remove(messageName);
										placeArray.set(i, currentState.getEndStateName(innerList.get(k)));
										synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//										printPlaceArray(placeArray);
										examineQueue.add(new ExamineMission(innerList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
										break;
									}
								}
								break;
							case NON_INNER_MESSAGE:
								List<String> sendList = currentState.getSendMessageList();
								// List<String>
								// receiveList=currentState.getReceiveMessageList();
//								System.out.println("44 " + sendList.size());
								for (int k = 0; k < sendList.size(); k++) {
									if(sendList.get(k).equals(messageName)){
										ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
										leftMessage.remove(messageName);
										placeArray.set(i, currentState.getEndStateName(sendList.get(k)));
										synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//										printPlaceArray(placeArray);
										examineQueue.add(new ExamineMission(sendList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
										break;
									}
								}
								List<String> receiveList=currentState.getReceiveMessageList();
								for (int k = 0; k < receiveList.size(); k++) {
									if(receiveList.get(k).equals(messageName)){
										ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
										leftMessage.remove(messageName);
										placeArray.set(i, currentState.getEndStateName(receiveList.get(k)));
										synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//										printPlaceArray(placeArray);
										examineQueue.add(new ExamineMission(receiveList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
										break;
									}
								}
								break;
							default:
									System.out.println("default");
//									innerList = currentState.getInnerMessageList();
////									System.out.println("33 " + innerList.size());
//									for (int k = 0; k < innerList.size(); k++) {// 
//										if(innerList.get(k).equals(messageName)){
//											ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
//											leftMessage.remove(messageName);
//											placeArray.set(i, currentState.getEndStateName(innerList.get(k)));
////											printPlaceArray(placeArray);
//											examineQueue.add(new ExamineMission(innerList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
//										}
//									}
//									sendList = currentState.getSendMessageList();
//									// List<String>
//									// receiveList=currentState.getReceiveMessageList();
////									System.out.println("44 " + sendList.size());
//									for (int k = 0; k < sendList.size(); k++) {
//										if(sendList.get(k).equals(messageName)){
//											ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
//											leftMessage.remove(messageName);
//											placeArray.set(i, currentState.getEndStateName(sendList.get(k)));
////											printPlaceArray(placeArray);
//											examineQueue.add(new ExamineMission(sendList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));									
//										}
//									}
							}
							
//							System.out.println("55 " + queue.size());
							break;//�ҵ��˵�һ����ʼ״̬�����������ٽ�����ѭ��
						}
					}
				}
				
			
		if(examineQueue.isEmpty()){//��һ����Ϣ��û��ƥ�䣬��Ȼ�˳�
			System.out.println("not find because of the first message name!");
			System.out.println("The execution time of examine the message sequence is: "+ (System.nanoTime() - currentTime) / 1000 + " us");
			return;
		}
		
		while(!examineQueue.isEmpty()){
				ExamineMission m = examineQueue.poll();// �Ӷ���ȡ����ɾ
				messageSeq=m.getList();
//				System.out.println(examineQueue.size()+" ***");
				boolean isFind=false;
				if (messageSeq.size()!=0) {// ��û�ж���ϸ�������Ϣ����	
//					System.out.println(messageSeq.size());
					messageName=messageSeq.get(0);
					messageType=AnalyseMessageType(messageName);//�ж��������Ϣ�����ͣ��Լ��Ƿ�Ϸ�
					ArrayList<String> placeArray = m.getPlaceList();
					for (int i = 0; i < timedAutomataStateList.size(); i++) {
						List<State> timedAutomataState = timedAutomataStateList.get(i);
						for (int j = 0; j < timedAutomataState.size(); j++) {// ������һ״̬�����ֲ���timedAutomataState
							State currentState = timedAutomataState.get(j);
//							System.out.println("88 "+currentState.getStateName());
//							System.out.println("$$$ "+currentState.getStateName());
//							System.out.println("$$$$$ "+placeArray.get(i));
							if (currentState.getStateName().equals(placeArray.get(i))) {// ���Զ���i���ҵ��˶�Ӧ���ֵ�state
								switch(messageType){
								case INNER_MESSAGE:
									List<String> innerList = currentState.getInnerMessageList();
//									System.out.println("99 " + innerList.size());
									for (int k = 0; k < innerList.size(); k++){// 
//										System.out.println("&&& "+innerList.get(k));
										if(innerList.get(k).equals(messageName)){
											isFind=true;
											ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
											leftMessage.remove(messageName);
											placeArray.set(i, currentState.getEndStateName(innerList.get(k)));
											synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//											printPlaceArray(placeArray);
											examineQueue.add(new ExamineMission(innerList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
											break;
										}
									}
									break;
								case NON_INNER_MESSAGE:
									List<String> sendList = currentState.getSendMessageList();
									List<String> receiveList = currentState
											.getReceiveMessageList();
//									System.out.println("991 " + sendList.size());
									for (int k = 0; k < sendList.size(); k++){
//										System.out.println("%%% "+sendList.get(k));
										if(sendList.get(k).equals(messageName))
										{
											isFind=true;
											ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
											leftMessage.remove(messageName);
											placeArray.set(i, currentState.getEndStateName(sendList.get(k)));
											synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//											printPlaceArray(placeArray);
											examineQueue.add(new ExamineMission(sendList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
											break;
										}
									}
									for (int k = 0; k < receiveList.size(); k++){
//										System.out.println("%%%% "+receiveList.get(k));
										if(receiveList.get(k).equals(messageName))
										{
											isFind=true;
											ArrayList<String> leftMessage=(ArrayList<String>)messageSeq.clone();
											leftMessage.remove(messageName);
											placeArray.set(i, currentState.getEndStateName(receiveList.get(k)));
											synchronizedMoveByMessageSeq(messageName,messageType,placeArray,i);//ȷ�������Զ���Ҳ�������Ϣ���������ƶ�
//											printPlaceArray(placeArray);
											examineQueue.add(new ExamineMission(receiveList.get(k),(ArrayList<String>)leftMessage.clone(), (ArrayList<String>) placeArray.clone()));
											break;
										}
									}
									break;
								}																
//								System.out.println("00 " + queue.size());
								break;//�ҵ��ˣ��Ͳ���ѭ���ˣ���Ϊͬһ��״̬û�еڶ����洢λ��
							}
						}						
					}
					if(!isFind&&(examineQueue.size()==0)){//û���ҵ������Ҷ�����Ҳû�������ˣ�ƥ��ʧ�ܣ��˳�
						System.out.println("Not Find!");
						System.out.println("The execution time of examine the message sequence is: "+ (System.nanoTime() - currentTime) / 1000 + " us");
						return;
					}
					
				}
				else  {// ���жϵ���Ϣ���ж��ж����ˣ��ɹ�
					System.out.println("--- The Message Sequence has been found in automachine! ---");
					System.out.println("The execution time of examine the message sequence is: "+ (System.nanoTime() - currentTime) / 1000 + " us");
					return;
				}
			}
		
			System.out.println("*** The Message Sequence has been found in automachine! ***");	
			System.out.println("The execution time of examine the message sequence is: "+ (System.nanoTime() - currentTime) / 1000 + " us");
		
	}

	public static void executeVerificationByKStepsRealTimeWithMultiAutomata(// ��messageview���̵߳���
			List<String> messageString) {
//		System.out.println("executeVerificationByKStepsRealTimeWithMultiAutomata 1");
//		if (kSteps == -1) {
//			return;
//		}
////		System.out.println("executeVerificationByKStepsRealTimeWithMultiAutomata 2");
//		//System.out.println("tjf tjf tjf " + timedAutomataState.size());
//		// if (timedAutomataStateList.size() == 0 &&
//		// currentProcessStateList.size() == 0) {//tjf
//		if (timedAutomataStateList.size() == 0) {
//			if (IImageKeys.automataFilesName.size() > 0) {
//				long currentTime1 = System.nanoTime();
//
//				InitilizeAutomataStateWithMultiAutomata(IImageKeys.automataFilesName);
//
//				System.out.println();
//				System.out
//						.println("The execution time of InitilizeAutomataState is: "
//								+ (System.nanoTime() - currentTime1)
//								/ 1000
//								+ " us");
//				System.out
//						.println("------------------------------------------------------------------------");
//
//			}
//			return;
//		}
//
//		long currentTime1 = System.nanoTime();
//
//		if (kSteps > 0) {
//			System.out.println();
//			System.out.println("Look ahead "
//					+ kSteps
//					+ " steps "
//					+ (withControllability != 0 ? "with" : "without")
//					+ " controllability by "
//					+ (isDepthSearchFirst != 0 ? "breadth search first."
//							: "depth search first."));
//			System.out.println();
//
//		}
//		GetMessageFromMessageSequence(messageString);
//		// AnalyzingWithoutTimeRealTime();
//		 AyalyzingWithoutTimePreLookAheadWithMultiAutomata();//tjf
//		
////		AyalyzingPreLookAheadWithMultiAutomata();
//
//		System.out.println();
//		if (kSteps > 0) {
//			// System.out.println("Look ahead " + kSteps + " steps " +
//			// (withControllability!=0?"with":"without") +
//			// " controllability by " +
//			// (isDepthSearchFirst!=0?"breadth search first.":"depth search first."));
//			System.out
//					.println("The execution time of analysis with Pre-Lookahead is: "
//							+ (System.nanoTime() - currentTime1) / 1000 + " us");
//		} else {
//			System.out
//					.println("The execution time of analysis without Pre-Lookahead is: "
//							+ (System.nanoTime() - currentTime1) / 1000 + " us");
//
//		}
//		System.out
//				.println("------------------------------------------------------------------------");
		
		

	}

	public static void executeVerificationByKStepsRealTime(
			List<String> messageString) {
		if (kSteps == -1) {
			return;
		}

		if (timedAutomataState.size() == 0 && currentProcessState == null) {
			if (IImageKeys.automataFilesName.size() > 0) {
				long currentTime1 = System.nanoTime();

				InitilizeAutomataState(IImageKeys.automataFilesName.get(0));

				System.out.println();
				System.out
						.println("The execution time of InitilizeAutomataState is: "
								+ (System.nanoTime() - currentTime1)
								/ 1000
								+ " us");
				System.out
						.println("------------------------------------------------------------------------");

			}
			return;
		}

		long currentTime1 = System.nanoTime();

		if (kSteps > 0) {
			System.out.println();
			System.out.println("Look ahead "
					+ kSteps
					+ " steps "
					+ (withControllability != 0 ? "with" : "without")
					+ " controllability by "
					+ (isDepthSearchFirst != 0 ? "breadth search first."
							: "depth search first."));
			System.out.println();

		}
		GetMessageFromMessageSequence(messageString);
		// AnalyzingWithoutTimeRealTime();
		AyalyzingWithoutTimePreLookAhead();

		System.out.println();
		if (kSteps > 0) {
			// System.out.println("Look ahead " + kSteps + " steps " +
			// (withControllability!=0?"with":"without") +
			// " controllability by " +
			// (isDepthSearchFirst!=0?"breadth search first.":"depth search first."));
			System.out
					.println("The execution time of analysis with Pre-Lookahead is: "
							+ (System.nanoTime() - currentTime1) / 1000 + " us");
		} else {
			System.out
					.println("The execution time of analysis without Pre-Lookahead is: "
							+ (System.nanoTime() - currentTime1) / 1000 + " us");

		}
		System.out
				.println("------------------------------------------------------------------------");

	}

	public static void InitilizeAutomataStateWithMultiAutomata(
			List<String> automataFilesName) {
		SetTimedAutomataSetWithMultiAutomata(automataFilesName);// ����������ڳ�ʼ��TimedAutomataSet��������һ���Զ����ļ���������Ŀ��timedAutomataSetList������ɸ�TimedAutomataSet
		GetTimedAutomataStateWithMultiAutomata();
		//
		// System.out.println(timedAutomataState.size());
		//
		// for (int i = 0; i < timedAutomataState.size(); i++) {
		// System.out.print(timedAutomataState.get(i).getStateName() + " [");
		// for (int j = 0; j < timedAutomataState.get(i).getEndStateList()
		// .size(); j++) {
		// System.out.print(timedAutomataState.get(i).getEndStateList()
		// .get(j).getStateName()
		// + " ");
		// }
		// System.out.println("]");
		//
		// }
		//
		// for (int i = 0; i < acceptState.size(); i++) {
		// System.out.print(acceptState.get(i).getStateName() + " ");
		// }
		// System.out.println();
		//
		// for (int i = 0; i < errorState.size(); i++) {
		// System.out.print(errorState.get(i).getStateName() + " ");
		// }
		// System.out.println();

		// ComputeLookAhead();
		// ComputeControllabilityWithMultiAutomata();//tjf
	}

	public static void InitilizeAutomataState(String automataFileName) {// tiaobudao
		SetTimedAutomataSet(automataFileName);
		GetTimedAutomataState();
		//
		// System.out.println(timedAutomataState.size());
		//
		// for (int i = 0; i < timedAutomataState.size(); i++) {
		// System.out.print(timedAutomataState.get(i).getStateName() + " [");
		// for (int j = 0; j < timedAutomataState.get(i).getEndStateList()
		// .size(); j++) {
		// System.out.print(timedAutomataState.get(i).getEndStateList()
		// .get(j).getStateName()
		// + " ");
		// }
		// System.out.println("]");
		//
		// }
		//
		// for (int i = 0; i < acceptState.size(); i++) {
		// System.out.print(acceptState.get(i).getStateName() + " ");
		// }
		// System.out.println();
		//
		// for (int i = 0; i < errorState.size(); i++) {
		// System.out.print(errorState.get(i).getStateName() + " ");
		// }
		// System.out.println();

		// ComputeLookAhead();
		ComputeControllability();
	}

	public static void executeVerificationByKSteps(String messageFileName,
			String automataFileName, Integer steps) {// ������

		kSteps = steps;

		GetMessageFromMessageLog(messageFileName);

		// for ( int i = 0 ; i < messageLog.size(); i++)
		// {
		// System.out.println(i + " "+messageLog.get(i).getMessageFullText());
		// }

		SetTimedAutomataSet(automataFileName);

		// for ( int i = 0 ; i < timedAutomataSet.getTimedAutomata().size();i++)
		// {
		// System.out.println(timedAutomataSet.getTimedAutomata().get(i).getStartStatus()
		// + " " + timedAutomataSet.getTimedAutomata().get(i).getEndStatus());
		// }

		double propertyTime = 0;

		GetTimedAutomataState();

		// System.out.println(timedAutomataState.size());
		//
		// for ( int i = 0 ; i < timedAutomataState.size();i++)
		// {
		// System.out.print(timedAutomataState.get(i).getStateName()+ " [");
		// for( int j = 0; j <
		// timedAutomataState.get(i).getEndStateList().size(); j++)
		// {
		// System.out.print(timedAutomataState.get(i).getEndStateList().get(j).getStateName()+" ");
		// }
		// System.out.println("]");
		//
		// }

		// for( int i = 0 ; i < acceptState.size(); i++)
		// {
		// System.out.print(acceptState.get(i).getStateName() + " ");
		// }
		// System.out.println();
		//
		//
		// for( int i = 0 ; i < errorState.size(); i++)
		// {
		// System.out.print(errorState.get(i).getStateName() + " ");
		// }
		// System.out.println();

		System.out.println("Analyzing with " + steps
				+ " step Lookahead started!");
		System.out.println();
		long currentTime1 = System.nanoTime();

		ComputeLookAhead();

		// for( int i = 0 ; i < timedAutomataState.size(); i++)
		// {
		// State state = timedAutomataState.get(i);
		//
		// System.out.print(state.getStateName() + " ");
		//
		// for(int k = 0; k < state.getErrorStateInfo().size(); k++)
		// {
		// System.out.print(state.getErrorStateInfo().get(k).getStateName()+"("+state.getErrorStateSteps().get(k)+",(");
		// for( int l = state.getErrorStateRoute().get(k).size() -1; l >= 0;l--)
		// {
		// System.out.print(state.getErrorStateRoute().get(k).get(l).getStateName()+" ");
		// }
		// System.out.print("))  ");
		// }
		//
		// for(int k = 0; k < state.getAcceptStateInfo().size(); k++)
		// {
		// System.out.print(state.getAcceptStateInfo().get(k).getStateName()+"("+state.getAcceptStateSteps().get(k)+",(");
		// for( int l = state.getAcceptStateRoute().get(k).size() -1; l >=
		// 0;l--)
		// {
		// System.out.print(state.getAcceptStateRoute().get(k).get(l).getStateName()+" ");
		// }
		// System.out.print("))  ");
		// }
		// System.out.println();
		// }

		AnalyzingWithoutTime(steps);

		// Analyzing(messageLog, timedAutomataSet, timedAutomataState,
		// acceptState, errorState);

		long currentTime2 = System.nanoTime();
		propertyTime = currentTime2 - currentTime1;
		System.out.println();
		System.out
				.println("The execution time of analysis with Pre-Lookahead is: "
						+ propertyTime / 1000 + " us");
		System.out
				.println("---------------------------------------------------------------");
	}

	public static void executeVerification(String messageFileName,
			String automataFileName) {

		GetMessageFromMessageLog(messageFileName);

		// for ( int i = 0 ; i < messageLog.size(); i++)
		// {
		// System.out.println(i + " "+messageLog.get(i).getMessageFullText());
		// }

		SetTimedAutomataSet(automataFileName);

		// for ( int i = 0 ; i < timedAutomataSet.getTimedAutomata().size();i++)
		// {
		// System.out.println(timedAutomataSet.getTimedAutomata().get(i).getStartStatus()
		// + " " + timedAutomataSet.getTimedAutomata().get(i).getEndStatus());
		// }

		double propertyTime = 0;

		GetTimedAutomataState();

		// System.out.println(timedAutomataState.size());
		//
		// for ( int i = 0 ; i < timedAutomataState.size();i++)
		// {
		// System.out.print(timedAutomataState.get(i).getStateName()+ " [");
		// for( int j = 0; j <
		// timedAutomataState.get(i).getEndStateList().size(); j++)
		// {
		// System.out.print(timedAutomataState.get(i).getEndStateList().get(j).getStateName()+" ");
		// }
		// System.out.println("]");
		// }
		//
		// for( int i = 0 ; i < acceptState.size(); i++)
		// {
		// System.out.print(acceptState.get(i).getStateName() + " ");
		// }
		// System.out.println();
		//
		//
		// for( int i = 0 ; i < errorState.size(); i++)
		// {
		// System.out.print(errorState.get(i).getStateName() + " ");
		// }
		// System.out.println();
		//
		// ComputeLookAhead(timedAutomataState,acceptState,errorState);
		//
		// for( int i = 0 ; i < timedAutomataState.size(); i++)
		// {
		// State state = timedAutomataState.get(i);
		//
		// System.out.print(state.getStateName() + " ");
		//
		// for(int k = 0; k < state.getErrorStateInfo().size(); k++)
		// {
		// System.out.print(state.getErrorStateInfo().get(k).getStateName()+"("+state.getErrorStateSteps().get(k)+",(");
		// for( int l = state.getErrorStateRoute().get(k).size() -1; l >= 0;l--)
		// {
		// System.out.print(state.getErrorStateRoute().get(k).get(l).getStateName()+" ");
		// }
		// System.out.print("))  ");
		// }
		//
		// for(int k = 0; k < state.getAcceptStateInfo().size(); k++)
		// {
		// System.out.print(state.getAcceptStateInfo().get(k).getStateName()+"("+state.getAcceptStateSteps().get(k)+",(");
		// for( int l = state.getAcceptStateRoute().get(k).size() -1; l >=
		// 0;l--)
		// {
		// System.out.print(state.getAcceptStateRoute().get(k).get(l).getStateName()+" ");
		// }
		// System.out.print("))  ");
		// }
		// System.out.println();
		// }

		System.out.println("Analyze with time");
		long currentTime1 = System.nanoTime();

		AnalyzingWithTime();

		// Analyzing(messageLog, timedAutomataSet, timedAutomataState,
		// acceptState, errorState);

		long currentTime2 = System.nanoTime();
		propertyTime = currentTime2 - currentTime1;
		System.out.println();
		System.out
				.println("The execution time of analysis with time condition is: "
						+ propertyTime / 1000 + " us");
		System.out
				.println("---------------------------------------------------------------");
	}

	public static void executeVerification(String messageFileName,
			List<String> automataFilesName) {
		String automataFileName = automataFilesName.get(0);

		GetMessageFromMessageLog(messageFileName);

		// for ( int i = 0 ; i < messageLog.size(); i++)
		// {
		// System.out.println(i + " "+messageLog.get(i).getMessageFullText());
		// }

		SetTimedAutomataSet(automataFileName);

		// for ( int i = 0 ; i < timedAutomataSet.getTimedAutomata().size();i++)
		// {
		// System.out.println(timedAutomataSet.getTimedAutomata().get(i).getStartStatus()
		// + " " + timedAutomataSet.getTimedAutomata().get(i).getEndStatus());
		// }

		double propertyTime = 0;

		GetTimedAutomataState();

		// System.out.println(timedAutomataState.size());
		//
		// for ( int i = 0 ; i < timedAutomataState.size();i++)
		// {
		// System.out.print(timedAutomataState.get(i).getStateName()+ " [");
		// for( int j = 0; j <
		// timedAutomataState.get(i).getEndStateList().size(); j++)
		// {
		// System.out.print(timedAutomataState.get(i).getEndStateList().get(j).getStateName()+" ");
		// }
		// System.out.println("]");
		// }
		//
		// for( int i = 0 ; i < acceptState.size(); i++)
		// {
		// System.out.print(acceptState.get(i).getStateName() + " ");
		// }
		// System.out.println();
		//
		//
		// for( int i = 0 ; i < errorState.size(); i++)
		// {
		// System.out.print(errorState.get(i).getStateName() + " ");
		// }
		// System.out.println();
		//
		// ComputeLookAhead(timedAutomataState,acceptState,errorState);
		//
		// for( int i = 0 ; i < timedAutomataState.size(); i++)
		// {
		// State state = timedAutomataState.get(i);
		//
		// System.out.print(state.getStateName() + " ");
		//
		// for(int k = 0; k < state.getErrorStateInfo().size(); k++)
		// {
		// System.out.print(state.getErrorStateInfo().get(k).getStateName()+"("+state.getErrorStateSteps().get(k)+",(");
		// for( int l = state.getErrorStateRoute().get(k).size() -1; l >= 0;l--)
		// {
		// System.out.print(state.getErrorStateRoute().get(k).get(l).getStateName()+" ");
		// }
		// System.out.print("))  ");
		// }
		//
		// for(int k = 0; k < state.getAcceptStateInfo().size(); k++)
		// {
		// System.out.print(state.getAcceptStateInfo().get(k).getStateName()+"("+state.getAcceptStateSteps().get(k)+",(");
		// for( int l = state.getAcceptStateRoute().get(k).size() -1; l >=
		// 0;l--)
		// {
		// System.out.print(state.getAcceptStateRoute().get(k).get(l).getStateName()+" ");
		// }
		// System.out.print("))  ");
		// }
		// System.out.println();
		// }

		System.out.println("Analyze with time");
		long currentTime1 = System.nanoTime();

		AnalyzingWithTime();

		// Analyzing(messageLog, timedAutomataSet, timedAutomataState,
		// acceptState, errorState);

		long currentTime2 = System.nanoTime();
		propertyTime = currentTime2 - currentTime1;
		System.out.println();
		System.out
				.println("The execution time of analysis with time condition is: "
						+ propertyTime / 1000 + " us");
		System.out
				.println("---------------------------------------------------------------");
	}

	public static void ComputeLookAhead() {// ��ӵ�����
		// ���ȣ�Ϊerror״̬��accept״̬�����Ӧ��errorStateInfo��acceptStateInfo
		List<State> lastStates = new ArrayList<State>();
		List<State> currentStates = new ArrayList<State>();

		// ��ֹ״̬һ����error״̬��accept״̬
		for (int i = 0; i < acceptState.size(); i++) {
			acceptState.get(i).addAcceptStateInfo(acceptState.get(i));
			acceptState.get(i).addAcceptStateSteps(0);
			List<State> route = new ArrayList<State>();
			route.add(acceptState.get(i));
			acceptState.get(i).addAcceptStateRoute(route);
			lastStates.add(acceptState.get(i));
		}

		for (int i = 0; i < errorState.size(); i++) {
			errorState.get(i).addErrorStateInfo(errorState.get(i));
			errorState.get(i).addErrorStateSteps(0);
			List<State> route = new ArrayList<State>();
			route.add(errorState.get(i));
			errorState.get(i).addErrorStateRoute(route);
			lastStates.add(errorState.get(i));
		}

		while (!lastStates.isEmpty()) {
			for (int i = 0; i < timedAutomataState.size(); i++) {
				State state = timedAutomataState.get(i);
				if (!state.contain(errorState) && !state.contain(acceptState)) {
					for (int j = 0; j < state.getEndStateList().size(); j++) {
						State nextState = state.getEndStateList().get(j);
						// ��һ��������ֹ��ѭ��
						if ((!state.getStateName().equals(
								nextState.getStateName()))
								&& nextState.contain(lastStates)) {
							for (int k = 0; k < nextState.getErrorStateInfo()
									.size(); k++) {
								List<State> route = new ArrayList<State>();
								List<State> lastRoute = nextState
										.getErrorStateRoute().get(k);
								for (int l = 0; l < lastRoute.size(); l++) {
									route.add(lastRoute.get(l));
								}
								route.add(state);

								if (!state.containErrorRoute(route)) {
									state.addErrorStateInfo(nextState
											.getErrorStateInfo().get(k));
									state.addErrorStateSteps(nextState
											.getErrorStateSteps().get(k) + 1);
									state.addErrorStateRoute(route);
								}
							}

							for (int k = 0; k < nextState.getAcceptStateInfo()
									.size(); k++) {
								List<State> route = new ArrayList<State>();
								List<State> lastRoute = nextState
										.getAcceptStateRoute().get(k);
								for (int l = 0; l < lastRoute.size(); l++) {
									route.add(lastRoute.get(l));
								}
								route.add(state);

								if (!state.containAcceptRoute(route)) {
									state.addAcceptStateInfo(nextState
											.getAcceptStateInfo().get(k));
									state.addAcceptStateSteps(nextState
											.getAcceptStateSteps().get(k) + 1);
									state.addAcceptStateRoute(route);
								}
							}

							currentStates.add(state);
						}

					}
				}
			}
			lastStates.clear();
			for (int i = 0; i < currentStates.size(); i++) {
				lastStates.add(currentStates.get(i));
			}
			currentStates.clear();
		}

		for (int i = 0; i < timedAutomataState.size(); i++) {
			State currentState = timedAutomataState.get(i);
			if (currentState.contain(acceptState)) {
				currentState.setControllableStatus(1);
				continue;
			}

			if (currentState.contain(errorState)) {
				currentState.setControllableStatus(0);
				continue;
			}

			for (int k = 0; k < currentState.getAcceptStateInfo().size(); k++) {
				if (currentState.getAcceptStateSteps().get(k) != 0) {
					currentState.setControllableStatus(1);
					break;
				}
			}

			if (currentState.getControllableStatus() != 1) {
				for (int k = 0; k < currentState.getErrorStateInfo().size(); k++) {
					if (currentState.getErrorStateSteps().get(k) != 0) {
						currentState.setControllableStatus(0);
						break;
					}
				}
			}
		}
	}

	public static void ComputeControllabilityWithMultiAutomata() {// �ᱻinitial��ͷ���Ǹ���������

		for (int no = 0; no < IImageKeys.automataFilesName.size(); no++) {
			// TimedAutomataSet timedAutomataSet = timedAutomataSetList.get(no);
			List<State> timedAutomataState = timedAutomataStateList.get(no);
			List<State> acceptState = acceptStateList.get(no);
			List<State> errorState = errorStateList.get(no);

			// ���ȣ�Ϊerror״̬��accept״̬�����Ӧ��errorStateInfo��acceptStateInfo
			List<State> lastStates = new ArrayList<State>();
			List<State> currentStates = new ArrayList<State>();

			// ��ֹ״̬һ����error״̬��accept״̬�����Ƚ���Accept״̬��Ϊ��ǰ״̬
			// ����Accept��ControllableStatusΪ1����������״̬
			for (int i = 0; i < acceptState.size(); i++) {
				State state = acceptState.get(i);
				state.setControllableStatus(1);
				lastStates.add(state);
			}

			while (!lastStates.isEmpty()) {
				// ÿ�ζ�����״̬���б���������¸�״̬��lastStates�ڵ�״̬����ݴ�ȷ����ControllableStatus����ֵ
				for (int i = 0; i < timedAutomataState.size(); i++) {
					State state = timedAutomataState.get(i);

					// �����ų�Accept״̬��Error״̬�����Ҹ�״̬δȷ����ControllableStatus����ֵ
					if (!state.contain(errorState)
							&& !state.contain(acceptState)
							&& state.getControllableStatus() == -1) {

						for (int j = 0; j < state.getEndStateList().size(); j++) {
							State nextState = state.getEndStateList().get(j);

							// ��һ��������ֹ��ѭ�����ǿɴ��
							if ((!state.getStateName().equals(
									nextState.getStateName()))
									&& nextState.contain(lastStates)) {
								state.setControllableStatus(1);
								currentStates.add(state);
							}

						}
					}
				}
				lastStates.clear();
				for (int i = 0; i < currentStates.size(); i++) {
					lastStates.add(currentStates.get(i));
				}
				currentStates.clear();
			}

			// ���³�ʼ��
			lastStates.clear();
			currentStates.clear();

			// Error��ControllableStatusΪ0
			for (int i = 0; i < errorState.size(); i++) {
				State state = errorState.get(i);
				state.setControllableStatus(0);
				lastStates.add(state);
			}

			while (!lastStates.isEmpty()) {
				// ÿ�ζ�����״̬���б���������¸�״̬��lastStates�ڵ�״̬����ݴ�ȷ����ControllableStatus����ֵ
				for (int i = 0; i < timedAutomataState.size(); i++) {
					State state = timedAutomataState.get(i);

					// �����ų�Accept״̬��Error״̬�����Ҹ�״̬δȷ����ControllableStatus����ֵ
					if (!state.contain(errorState)
							&& !state.contain(acceptState)
							&& state.getControllableStatus() == -1) {

						for (int j = 0; j < state.getEndStateList().size(); j++) {
							State nextState = state.getEndStateList().get(j);

							// ��һ��������ֹ��ѭ������ֹ�ظ��������ǿɴ��
							if ((!state.getStateName().equals(
									nextState.getStateName()))
									&& !(state.getControllableStatus() == 0)
									&& nextState.contain(lastStates)) {
								state.setControllableStatus(0);
								currentStates.add(state);
							}

						}
					}
				}
				lastStates.clear();
				for (int i = 0; i < currentStates.size(); i++) {
					lastStates.add(currentStates.get(i));
				}
				currentStates.clear();
			}

			for (int i = 0; i < timedAutomataState.size(); i++) {
				State state = timedAutomataState.get(i);
				if (state.getControllableStatus() == 1) {
					System.out.println(state.getStateName() + " C");
				} else if (state.getControllableStatus() == 0) {
					System.out.println(state.getStateName() + " U");
				} else {
					System.out.println(state.getStateName() + " -");
				}
			}

		}
	}

	public static void ComputeControllability() {
		// ���ȣ�Ϊerror״̬��accept״̬�����Ӧ��errorStateInfo��acceptStateInfo
		List<State> lastStates = new ArrayList<State>();
		List<State> currentStates = new ArrayList<State>();

		// ��ֹ״̬һ����error״̬��accept״̬�����Ƚ���Accept״̬��Ϊ��ǰ״̬
		// ����Accept��ControllableStatusΪ1����������״̬
		for (int i = 0; i < acceptState.size(); i++) {
			State state = acceptState.get(i);
			state.setControllableStatus(1);
			lastStates.add(state);
		}

		while (!lastStates.isEmpty()) {
			// ÿ�ζ�����״̬���б���������¸�״̬��lastStates�ڵ�״̬����ݴ�ȷ����ControllableStatus����ֵ
			for (int i = 0; i < timedAutomataState.size(); i++) {
				State state = timedAutomataState.get(i);

				// �����ų�Accept״̬��Error״̬�����Ҹ�״̬δȷ����ControllableStatus����ֵ
				if (!state.contain(errorState) && !state.contain(acceptState)
						&& state.getControllableStatus() == -1) {

					for (int j = 0; j < state.getEndStateList().size(); j++) {
						State nextState = state.getEndStateList().get(j);

						// ��һ��������ֹ��ѭ�����ǿɴ��
						if ((!state.getStateName().equals(
								nextState.getStateName()))
								&& nextState.contain(lastStates)) {
							state.setControllableStatus(1);
							currentStates.add(state);
						}

					}
				}
			}
			lastStates.clear();
			for (int i = 0; i < currentStates.size(); i++) {
				lastStates.add(currentStates.get(i));
			}
			currentStates.clear();
		}

		// ���³�ʼ��
		lastStates.clear();
		currentStates.clear();

		// Error��ControllableStatusΪ0
		for (int i = 0; i < errorState.size(); i++) {
			State state = errorState.get(i);
			state.setControllableStatus(0);
			lastStates.add(state);
		}

		while (!lastStates.isEmpty()) {
			// ÿ�ζ�����״̬���б���������¸�״̬��lastStates�ڵ�״̬����ݴ�ȷ����ControllableStatus����ֵ
			for (int i = 0; i < timedAutomataState.size(); i++) {
				State state = timedAutomataState.get(i);

				// �����ų�Accept״̬��Error״̬�����Ҹ�״̬δȷ����ControllableStatus����ֵ
				if (!state.contain(errorState) && !state.contain(acceptState)
						&& state.getControllableStatus() == -1) {

					for (int j = 0; j < state.getEndStateList().size(); j++) {
						State nextState = state.getEndStateList().get(j);

						// ��һ��������ֹ��ѭ������ֹ�ظ��������ǿɴ��
						if ((!state.getStateName().equals(
								nextState.getStateName()))
								&& !(state.getControllableStatus() == 0)
								&& nextState.contain(lastStates)) {
							state.setControllableStatus(0);
							currentStates.add(state);
						}

					}
				}
			}
			lastStates.clear();
			for (int i = 0; i < currentStates.size(); i++) {
				lastStates.add(currentStates.get(i));
			}
			currentStates.clear();
		}

		for (int i = 0; i < timedAutomataState.size(); i++) {
			State state = timedAutomataState.get(i);
			if (state.getControllableStatus() == 1) {
				System.out.println(state.getStateName() + " C");
			} else if (state.getControllableStatus() == 0) {
				System.out.println(state.getStateName() + " U");
			} else {
				System.out.println(state.getStateName() + " -");
			}
		}
	}

	public static boolean LookAheadErrorState(State currentState, int steps) {
		for (int i = 0; i < currentState.getErrorStateSteps().size(); i++) {
			if (currentState.getErrorStateSteps().get(i) <= steps) {
				return false;
			}
		}
		return true;
	}

	public static boolean LookAheadAcceptState(State currentState, int steps) {
		for (int i = 0; i < currentState.getAcceptStateSteps().size(); i++) {
			if (currentState.getAcceptStateSteps().get(i) <= steps) {
				return false;
			}
		}
		return true;
	}



	public static boolean AyalyzingWithoutTimePreLookAheadWithMultiAutomata() {// �ᱻexecuteVerificationByKStepsRealTimeWithMultiAutomata����

		List<Message> messageInstances = new ArrayList<Message>();

		for (int l = 0; l < messageLog.size(); l++) {
			Message message = messageLog.get(l);
			messageInstances.add(message);
		}

		for (int no = 0; no < IImageKeys.automataFilesName.size(); no++) {
			State currentProcessState = currentProcessStateList.get(no);
			State nextProcessState = nextProcessStateList.get(no);
			List<State> timedAutomataState = timedAutomataStateList.get(no);
			List<State> acceptState = acceptStateList.get(no);
			List<State> errorState = errorStateList.get(no);
			boolean TACreated = false;

			int indexI = 0;

			Message currentI = messageInstances.get(indexI);

			if (currentProcessState.getStateName().equals(
					timedAutomataState.get(0).getStateName())) {
				// if (kSteps > 0) {
				// System.out.println("Current Message : "
				// + currentI.getMessageOrigin());
				// if( withControllability == 0 && isDepthSearchFirst == 0)
				// {
				// LookaheadWithoutControllabilityByDepthSearch(currentProcessState);
				// }
				// else if( withControllability == 0 && isDepthSearchFirst != 0)
				// {
				// LookaheadWithoutControllabilityByBreadthSearch(currentProcessState);
				// }
				// else if( withControllability != 0 && isDepthSearchFirst == 0)
				// {
				// LookaheadWithControllabilityByDepthSearch(currentProcessState);
				// }
				// else if( withControllability != 0 && isDepthSearchFirst != 0)
				// {
				// LookaheadWithControllabilityByBreadthSearch(currentProcessState);
				// }
				// }
				outer: while (currentI != null) {
					for (int i = 0; i < currentProcessState
							.getStateTimedCondition().size(); i++) {
						if (currentI.getMessageFullText().equals(
								currentProcessState.getStateTimedCondition()
										.get(i))) {
							nextProcessState = currentProcessState
									.getEndStateList().get(i);
							if (!nextProcessState.getStateName().equals(
									currentProcessState.getStateName())) {
								// if (!nextProcessState.contain(errorState)) {
								// sequentialObservation.add(true);
								// } else {
								// sequentialObservation.add(false);
								// //errorMessage.add(currentI.getMessageOrigin());
								// }
								if (kSteps > 0) {
									System.out.println("Current Message : "
											+ currentI.getMessageOrigin());
									if (nextProcessState.contain(acceptState)) {
										System.out.println("Current state : "
												+ nextProcessState
														.getStateName()
												+ "(Accept)");
										System.out.println();
									} else if (nextProcessState
											.contain(errorState)) {
										System.out.println("Current state : "
												+ nextProcessState
														.getStateName()
												+ "(Error)");
										System.out.println();
									} else {
										if (withControllability == 0
												&& isDepthSearchFirst == 0) {
											LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
										} else if (withControllability == 0
												&& isDepthSearchFirst != 0) {
											LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
										} else if (withControllability != 0
												&& isDepthSearchFirst == 0) {
											LookaheadWithControllabilityByDepthSearch(nextProcessState);
										} else if (withControllability != 0
												&& isDepthSearchFirst != 0) {
											LookaheadWithControllabilityByBreadthSearch(nextProcessState);
										}
									}
								}
							}
							break outer;
						}

					}

					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
				}
			}

			if (currentI != null && TACreated == false) {
				TACreated = true;
				currentI = messageInstances.get(++indexI);

				currentProcessState = nextProcessState;
				currentProcessStateList.set(no, currentProcessState);

				while (currentI != null) {
					outer2: for (int i = 0; i < currentProcessState
							.getStateTimedCondition().size(); i++) {

						if (currentI.getMessageFullText().equals(
								currentProcessState.getStateTimedCondition()
										.get(i))) {
							nextProcessState = currentProcessState
									.getEndStateList().get(i);
							if (!nextProcessState.getStateName().equals(
									currentProcessState.getStateName())) {
								// if (!nextProcessState.contain(errorState)) {
								// sequentialObservation.add(true);
								// } else {
								// sequentialObservation.add(false);
								// //errorMessage.add(currentI.getMessageOrigin());
								// }
								if (kSteps > 0) {
									System.out.println("Current Message : "
											+ currentI.getMessageOrigin());
									if (nextProcessState.contain(acceptState)) {
										System.out.println("Current state : "
												+ nextProcessState
														.getStateName()
												+ "(Accept)");
										System.out.println();
									} else if (nextProcessState
											.contain(errorState)) {
										System.out.println("Current state : "
												+ nextProcessState
														.getStateName()
												+ "(Error)");
										System.out.println();
									} else {
										if (withControllability == 0
												&& isDepthSearchFirst == 0) {
											LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
										} else if (withControllability == 0
												&& isDepthSearchFirst != 0) {
											LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
										} else if (withControllability != 0
												&& isDepthSearchFirst == 0) {
											LookaheadWithControllabilityByDepthSearch(nextProcessState);
										} else if (withControllability != 0
												&& isDepthSearchFirst != 0) {
											LookaheadWithControllabilityByBreadthSearch(nextProcessState);
										}
									}
								}
							}
							break outer2;
						}
					}

					// if (!currentProcessState.getStateName().equals(
					// nextProcessState.getStateName())) {
					// if (kSteps > 0) {
					// LookAhead(currentProcessState);
					// }
					// }
					currentProcessState = nextProcessState;
					currentProcessStateList.set(no, currentProcessState);

					if (currentProcessState.contain(acceptState)) {
						// if (kSteps > 0) {
						// System.out.println("Current Message : "
						// + currentI.getMessageOrigin());
						// System.out.println("Current state : " +
						// currentProcessState.getStateName()
						// + "(Accept)");
						// }
						// acceptMessage.add(indexI);
						if ((indexI + 1) < messageInstances.size()) {

							currentProcessState = timedAutomataState.get(0);
							currentProcessStateList
									.set(no, currentProcessState);
							currentI = messageInstances.get(++indexI);

							if (kSteps > 0) {
								System.out.println("Current Message : "
										+ currentI.getMessageOrigin());
								if (withControllability == 0
										&& isDepthSearchFirst == 0) {
									LookaheadWithoutControllabilityByDepthSearch(currentProcessState);
								} else if (withControllability == 0
										&& isDepthSearchFirst != 0) {
									LookaheadWithoutControllabilityByBreadthSearch(currentProcessState);
								} else if (withControllability != 0
										&& isDepthSearchFirst == 0) {
									LookaheadWithControllabilityByDepthSearch(currentProcessState);
								} else if (withControllability != 0
										&& isDepthSearchFirst != 0) {
									LookaheadWithControllabilityByBreadthSearch(currentProcessState);
								}
							}

							outer3: while (currentI != null) {
								for (int k = 0; k < currentProcessState
										.getStateTimedCondition().size(); k++) {
									if (currentI.getMessageFullText().equals(
											currentProcessState
													.getStateTimedCondition()
													.get(k))) {
										nextProcessState = currentProcessState
												.getEndStateList().get(k);
										if (!nextProcessState.getStateName()
												.equals(currentProcessState
														.getStateName())) {
											// if
											// (!nextProcessState.contain(errorState))
											// {
											// sequentialObservation.add(true);
											// } else {
											// sequentialObservation.add(false);
											// errorMessage.add(currentI.getMessageOrigin());
											// }
											if (kSteps > 0) {
												System.out
														.println("Current Message : "
																+ currentI
																		.getMessageOrigin());
												if (nextProcessState
														.contain(acceptState)) {
													System.out
															.println("Current state : "
																	+ nextProcessState
																			.getStateName()
																	+ "(Accept)");
													System.out.println();
												} else if (nextProcessState
														.contain(errorState)) {
													System.out
															.println("Current state : "
																	+ nextProcessState
																			.getStateName()
																	+ "(Error)");
													System.out.println();
												} else {
													if (withControllability == 0
															&& isDepthSearchFirst == 0) {
														LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
													} else if (withControllability == 0
															&& isDepthSearchFirst != 0) {
														LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
													} else if (withControllability != 0
															&& isDepthSearchFirst == 0) {
														LookaheadWithControllabilityByDepthSearch(nextProcessState);
													} else if (withControllability != 0
															&& isDepthSearchFirst != 0) {
														LookaheadWithControllabilityByBreadthSearch(nextProcessState);
													}
												}
											}
										}
										break outer3;
									}
								}
								if ((indexI + 1) < messageInstances.size())
									currentI = messageInstances.get(++indexI);
								else
									currentI = null;
							}

							if ((indexI + 1) < messageInstances.size())
								currentI = messageInstances.get(++indexI);
							else
								currentI = null;
							// currentProcessState = nextProcessState;

						} else
							currentI = null;
					} else if (currentProcessState.contain(errorState)) {
						currentProcessState = timedAutomataState.get(0);
						currentProcessStateList.set(no, currentProcessState);
						currentI = messageInstances.get(++indexI);
						if (kSteps > 0) {
							System.out.println("Current Message : "
									+ currentI.getMessageOrigin());
							if (withControllability == 0
									&& isDepthSearchFirst == 0) {
								LookaheadWithoutControllabilityByDepthSearch(currentProcessState);
							} else if (withControllability == 0
									&& isDepthSearchFirst != 0) {
								LookaheadWithoutControllabilityByBreadthSearch(currentProcessState);
							} else if (withControllability != 0
									&& isDepthSearchFirst == 0) {
								LookaheadWithControllabilityByDepthSearch(currentProcessState);
							} else if (withControllability != 0
									&& isDepthSearchFirst != 0) {
								LookaheadWithControllabilityByBreadthSearch(currentProcessState);
							}
						}

						outer3: while (currentI != null) {
							for (int k = 0; k < currentProcessState
									.getStateTimedCondition().size(); k++) {
								if (currentI.getMessageFullText().equals(
										currentProcessState
												.getStateTimedCondition()
												.get(k))) {

									nextProcessState = currentProcessState
											.getEndStateList().get(k);
									if (!nextProcessState.getStateName()
											.equals(currentProcessState
													.getStateName())) {
										// if
										// (!nextProcessState.contain(errorState))
										// {
										// sequentialObservation.add(true);
										// } else {
										// sequentialObservation.add(false);
										// errorMessage.add(currentI.getMessageOrigin());
										// }
										if (kSteps > 0) {
											System.out
													.println("Current Message : "
															+ currentI
																	.getMessageOrigin());
											if (nextProcessState
													.contain(acceptState)) {
												System.out
														.println("Current state : "
																+ nextProcessState
																		.getStateName()
																+ "(Accept)");
												System.out.println();
											} else if (nextProcessState
													.contain(errorState)) {
												System.out
														.println("Current state : "
																+ nextProcessState
																		.getStateName()
																+ "(Error)");
												System.out.println();
											} else {
												if (withControllability == 0
														&& isDepthSearchFirst == 0) {
													LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
												} else if (withControllability == 0
														&& isDepthSearchFirst != 0) {
													LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
												} else if (withControllability != 0
														&& isDepthSearchFirst == 0) {
													LookaheadWithControllabilityByDepthSearch(nextProcessState);
												} else if (withControllability != 0
														&& isDepthSearchFirst != 0) {
													LookaheadWithControllabilityByBreadthSearch(nextProcessState);
												}
											}
										}
									}

									break outer3;
								}
							}
							if ((indexI + 1) < messageInstances.size())
								currentI = messageInstances.get(++indexI);
							else
								currentI = null;
						}

						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
						// currentProcessState = nextProcessState;

					} else {
						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
					}
				}
				TACreated = false;
			}

		}

		return true;
	}

	public static boolean AyalyzingWithoutTimePreLookAhead() {// ��ӵ�����

		List<Message> messageInstances = new ArrayList<Message>();

		for (int l = 0; l < messageLog.size(); l++) {
			Message message = messageLog.get(l);
			messageInstances.add(message);
		}

		boolean TACreated = false;

		int indexI = 0;

		Message currentI = messageInstances.get(indexI);

		if (currentProcessState.getStateName().equals(
				timedAutomataState.get(0).getStateName())) {
			// if (kSteps > 0) {
			// System.out.println("Current Message : "
			// + currentI.getMessageOrigin());
			// if( withControllability == 0 && isDepthSearchFirst == 0)
			// {
			// LookaheadWithoutControllabilityByDepthSearch(currentProcessState);
			// }
			// else if( withControllability == 0 && isDepthSearchFirst != 0)
			// {
			// LookaheadWithoutControllabilityByBreadthSearch(currentProcessState);
			// }
			// else if( withControllability != 0 && isDepthSearchFirst == 0)
			// {
			// LookaheadWithControllabilityByDepthSearch(currentProcessState);
			// }
			// else if( withControllability != 0 && isDepthSearchFirst != 0)
			// {
			// LookaheadWithControllabilityByBreadthSearch(currentProcessState);
			// }
			// }
			outer: while (currentI != null) {
				for (int i = 0; i < currentProcessState
						.getStateTimedCondition().size(); i++) {
					if (currentI.getMessageFullText()
							.equals(currentProcessState
									.getStateTimedCondition().get(i))) {
						nextProcessState = currentProcessState
								.getEndStateList().get(i);
						if (!nextProcessState.getStateName().equals(
								currentProcessState.getStateName())) {
							// if (!nextProcessState.contain(errorState)) {
							// sequentialObservation.add(true);
							// } else {
							// sequentialObservation.add(false);
							// //errorMessage.add(currentI.getMessageOrigin());
							// }
							if (kSteps > 0) {
								System.out.println("Current Message : "
										+ currentI.getMessageOrigin());
								if (nextProcessState.contain(acceptState)) {
									System.out.println("Current state : "
											+ nextProcessState.getStateName()
											+ "(Accept)");
									System.out.println();
								} else if (nextProcessState.contain(errorState)) {
									System.out.println("Current state : "
											+ nextProcessState.getStateName()
											+ "(Error)");
									System.out.println();
								} else {
									if (withControllability == 0
											&& isDepthSearchFirst == 0) {
										LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
									} else if (withControllability == 0
											&& isDepthSearchFirst != 0) {
										LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
									} else if (withControllability != 0
											&& isDepthSearchFirst == 0) {
										LookaheadWithControllabilityByDepthSearch(nextProcessState);
									} else if (withControllability != 0
											&& isDepthSearchFirst != 0) {
										LookaheadWithControllabilityByBreadthSearch(nextProcessState);
									}
								}
							}
						}
						break outer;
					}

				}

				if ((indexI + 1) < messageInstances.size())
					currentI = messageInstances.get(++indexI);
				else
					currentI = null;
			}
		}

		if (currentI != null && TACreated == false) {
			TACreated = true;
			currentI = messageInstances.get(++indexI);

			currentProcessState = nextProcessState;

			while (currentI != null) {
				outer2: for (int i = 0; i < currentProcessState
						.getStateTimedCondition().size(); i++) {

					if (currentI.getMessageFullText()
							.equals(currentProcessState
									.getStateTimedCondition().get(i))) {
						nextProcessState = currentProcessState
								.getEndStateList().get(i);
						if (!nextProcessState.getStateName().equals(
								currentProcessState.getStateName())) {
							// if (!nextProcessState.contain(errorState)) {
							// sequentialObservation.add(true);
							// } else {
							// sequentialObservation.add(false);
							// //errorMessage.add(currentI.getMessageOrigin());
							// }
							if (kSteps > 0) {
								System.out.println("Current Message : "
										+ currentI.getMessageOrigin());
								if (nextProcessState.contain(acceptState)) {
									System.out.println("Current state : "
											+ nextProcessState.getStateName()
											+ "(Accept)");
									System.out.println();
								} else if (nextProcessState.contain(errorState)) {
									System.out.println("Current state : "
											+ nextProcessState.getStateName()
											+ "(Error)");
									System.out.println();
								} else {
									if (withControllability == 0
											&& isDepthSearchFirst == 0) {
										LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
									} else if (withControllability == 0
											&& isDepthSearchFirst != 0) {
										LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
									} else if (withControllability != 0
											&& isDepthSearchFirst == 0) {
										LookaheadWithControllabilityByDepthSearch(nextProcessState);
									} else if (withControllability != 0
											&& isDepthSearchFirst != 0) {
										LookaheadWithControllabilityByBreadthSearch(nextProcessState);
									}
								}
							}
						}
						break outer2;
					}
				}

				// if (!currentProcessState.getStateName().equals(
				// nextProcessState.getStateName())) {
				// if (kSteps > 0) {
				// LookAhead(currentProcessState);
				// }
				// }
				currentProcessState = nextProcessState;

				if (currentProcessState.contain(acceptState)) {
					// if (kSteps > 0) {
					// System.out.println("Current Message : "
					// + currentI.getMessageOrigin());
					// System.out.println("Current state : " +
					// currentProcessState.getStateName()
					// + "(Accept)");
					// }
					// acceptMessage.add(indexI);
					if ((indexI + 1) < messageInstances.size()) {

						currentProcessState = timedAutomataState.get(0);
						currentI = messageInstances.get(++indexI);

						if (kSteps > 0) {
							System.out.println("Current Message : "
									+ currentI.getMessageOrigin());
							if (withControllability == 0
									&& isDepthSearchFirst == 0) {
								LookaheadWithoutControllabilityByDepthSearch(currentProcessState);
							} else if (withControllability == 0
									&& isDepthSearchFirst != 0) {
								LookaheadWithoutControllabilityByBreadthSearch(currentProcessState);
							} else if (withControllability != 0
									&& isDepthSearchFirst == 0) {
								LookaheadWithControllabilityByDepthSearch(currentProcessState);
							} else if (withControllability != 0
									&& isDepthSearchFirst != 0) {
								LookaheadWithControllabilityByBreadthSearch(currentProcessState);
							}
						}

						outer3: while (currentI != null) {
							for (int k = 0; k < currentProcessState
									.getStateTimedCondition().size(); k++) {
								if (currentI.getMessageFullText().equals(
										currentProcessState
												.getStateTimedCondition()
												.get(k))) {
									nextProcessState = currentProcessState
											.getEndStateList().get(k);
									if (!nextProcessState.getStateName()
											.equals(currentProcessState
													.getStateName())) {
										// if
										// (!nextProcessState.contain(errorState))
										// {
										// sequentialObservation.add(true);
										// } else {
										// sequentialObservation.add(false);
										// errorMessage.add(currentI.getMessageOrigin());
										// }
										if (kSteps > 0) {
											System.out
													.println("Current Message : "
															+ currentI
																	.getMessageOrigin());
											if (nextProcessState
													.contain(acceptState)) {
												System.out
														.println("Current state : "
																+ nextProcessState
																		.getStateName()
																+ "(Accept)");
												System.out.println();
											} else if (nextProcessState
													.contain(errorState)) {
												System.out
														.println("Current state : "
																+ nextProcessState
																		.getStateName()
																+ "(Error)");
												System.out.println();
											} else {
												if (withControllability == 0
														&& isDepthSearchFirst == 0) {
													LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
												} else if (withControllability == 0
														&& isDepthSearchFirst != 0) {
													LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
												} else if (withControllability != 0
														&& isDepthSearchFirst == 0) {
													LookaheadWithControllabilityByDepthSearch(nextProcessState);
												} else if (withControllability != 0
														&& isDepthSearchFirst != 0) {
													LookaheadWithControllabilityByBreadthSearch(nextProcessState);
												}
											}
										}
									}
									break outer3;
								}
							}
							if ((indexI + 1) < messageInstances.size())
								currentI = messageInstances.get(++indexI);
							else
								currentI = null;
						}

						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
						// currentProcessState = nextProcessState;

					} else
						currentI = null;
				} else if (currentProcessState.contain(errorState)) {
					currentProcessState = timedAutomataState.get(0);
					currentI = messageInstances.get(++indexI);
					if (kSteps > 0) {
						System.out.println("Current Message : "
								+ currentI.getMessageOrigin());
						if (withControllability == 0 && isDepthSearchFirst == 0) {
							LookaheadWithoutControllabilityByDepthSearch(currentProcessState);
						} else if (withControllability == 0
								&& isDepthSearchFirst != 0) {
							LookaheadWithoutControllabilityByBreadthSearch(currentProcessState);
						} else if (withControllability != 0
								&& isDepthSearchFirst == 0) {
							LookaheadWithControllabilityByDepthSearch(currentProcessState);
						} else if (withControllability != 0
								&& isDepthSearchFirst != 0) {
							LookaheadWithControllabilityByBreadthSearch(currentProcessState);
						}
					}

					outer3: while (currentI != null) {
						for (int k = 0; k < currentProcessState
								.getStateTimedCondition().size(); k++) {
							if (currentI.getMessageFullText().equals(
									currentProcessState
											.getStateTimedCondition().get(k))) {

								nextProcessState = currentProcessState
										.getEndStateList().get(k);
								if (!nextProcessState.getStateName().equals(
										currentProcessState.getStateName())) {
									// if
									// (!nextProcessState.contain(errorState)) {
									// sequentialObservation.add(true);
									// } else {
									// sequentialObservation.add(false);
									// errorMessage.add(currentI.getMessageOrigin());
									// }
									if (kSteps > 0) {
										System.out.println("Current Message : "
												+ currentI.getMessageOrigin());
										if (nextProcessState
												.contain(acceptState)) {
											System.out
													.println("Current state : "
															+ nextProcessState
																	.getStateName()
															+ "(Accept)");
											System.out.println();
										} else if (nextProcessState
												.contain(errorState)) {
											System.out
													.println("Current state : "
															+ nextProcessState
																	.getStateName()
															+ "(Error)");
											System.out.println();
										} else {
											if (withControllability == 0
													&& isDepthSearchFirst == 0) {
												LookaheadWithoutControllabilityByDepthSearch(nextProcessState);
											} else if (withControllability == 0
													&& isDepthSearchFirst != 0) {
												LookaheadWithoutControllabilityByBreadthSearch(nextProcessState);
											} else if (withControllability != 0
													&& isDepthSearchFirst == 0) {
												LookaheadWithControllabilityByDepthSearch(nextProcessState);
											} else if (withControllability != 0
													&& isDepthSearchFirst != 0) {
												LookaheadWithControllabilityByBreadthSearch(nextProcessState);
											}
										}
									}
								}

								break outer3;
							}
						}
						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
					}

					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
					// currentProcessState = nextProcessState;

				} else {
					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
				}
			}
			TACreated = false;
		}
		return true;
	}

	private static void LookaheadWithControllabilityByBreadthSearch(State state) {
		int step = 1;
		List<State> lastState = new ArrayList<State>();
		List<State> currentState = new ArrayList<State>();
		// List<State> allState = new ArrayList<State>();
		List<List<State>> controllableRouteList = new ArrayList<List<State>>();
		List<List<State>> uncontrollableRouteList = new ArrayList<List<State>>();

		lastState.add(state);
		// allState.add(state);
		List<State> startState = new ArrayList<State>();
		startState.add(state);
		currentState.clear();
		if (state.getControllableStatus() == 0) {
			uncontrollableRouteList.add(startState);
		} else if (state.getControllableStatus() == 1) {
			controllableRouteList.add(startState);
		}

		while (step <= kSteps) {
			for (int i = 0; i < lastState.size(); i++) {
				State current = lastState.get(i);

				if (current.getControllableStatus() == 0) {// ���ɿ���
					// ���ݶ��壬���ɿ���״̬�ĺ��״̬��Ϊ���ɿ���
					for (int j = 0; j < current.getEndStateList().size(); j++) {
						State next = current.getEndStateList().get(j);
						// if (!next.contain(allState)) {
						// ��������ѭ���ͻ�·�Ŀ��ܣ������·�Ѿ����ڵģ�����ɾ������
						for (int k = 0; k < uncontrollableRouteList.size(); k++) {
							List<State> stateListTemp = uncontrollableRouteList
									.get(k);
							// if( stateListTemp.size() == step)
							// {
							if (stateListTemp.size() == step
									&& stateListTemp
											.get(stateListTemp.size() - 1)
											.getStateName()
											.equals(current.getStateName())) {
								List<State> newStateList = new ArrayList<State>();
								for (int m = 0; m < stateListTemp.size(); m++) {
									newStateList.add(stateListTemp.get(m));
								}
								newStateList.add(next);
								if (!isExist(newStateList,
										uncontrollableRouteList)) {
									uncontrollableRouteList.add(newStateList);
								}
								currentState.add(next);
							}
							// }
						}
						// }
					}
				} else if (current.getControllableStatus() == 1) {// �ɿ���
					// ���ݶ��壬�ɿ���״̬�ĺ��״̬�����ǿɿ��ƣ�Ҳ�����ǲ��ɿ���
					for (int j = 0; j < current.getEndStateList().size(); j++) {
						State next = current.getEndStateList().get(j);
						// ��������ѭ���ͻ�·�Ŀ��ܣ������·�Ѿ����ڵģ�����ɾ������
						// if (!next.contain(allState)) {
						if (next.getControllableStatus() == 0) {
							// ����������Ӳ��ɿؽ���ɿأ��Ӳ��ɿؽ��벻�ɿ�
							for (int k = 0; k < uncontrollableRouteList.size(); k++) {
								List<State> stateListTemp = uncontrollableRouteList
										.get(k);
								// if( stateListTemp.size() == step)
								// {
								if (stateListTemp.size() == step
										&& stateListTemp
												.get(stateListTemp.size() - 1)
												.getStateName()
												.equals(current.getStateName())) {
									List<State> newStateList = new ArrayList<State>();
									for (int m = 0; m < stateListTemp.size(); m++) {
										newStateList.add(stateListTemp.get(m));
									}
									newStateList.add(next);
									if (!isExist(newStateList,
											uncontrollableRouteList)) {
										uncontrollableRouteList
												.add(newStateList);
									}
									currentState.add(next);
									// allState.add(next);
								}
								// }
							}

							for (int k = 0; k < controllableRouteList.size(); k++) {
								List<State> stateListTemp = controllableRouteList
										.get(k);
								// if( stateListTemp.size() == step)
								// {
								if (stateListTemp.size() == step
										&& stateListTemp
												.get(stateListTemp.size() - 1)
												.getStateName()
												.equals(current.getStateName())) {
									List<State> newStateList = new ArrayList<State>();
									for (int m = 0; m < stateListTemp.size(); m++) {
										newStateList.add(stateListTemp.get(m));
									}
									newStateList.add(next);
									if (!isExist(newStateList,
											uncontrollableRouteList)) {
										uncontrollableRouteList
												.add(newStateList);
									}
									currentState.add(next);
									// allState.add(next);
								}
								// }
							}
						} else if (next.getControllableStatus() == 1) {
							for (int k = 0; k < controllableRouteList.size(); k++) {
								List<State> stateListTemp = controllableRouteList
										.get(k);
								// if( stateListTemp.size() == step)
								// {
								if (stateListTemp.size() == step
										&& stateListTemp
												.get(stateListTemp.size() - 1)
												.getStateName()
												.equals(current.getStateName())) {
									List<State> newStateList = new ArrayList<State>();
									for (int m = 0; m < stateListTemp.size(); m++) {
										newStateList.add(stateListTemp.get(m));
									}
									newStateList.add(next);
									if (!isExist(newStateList,
											controllableRouteList)) {
										controllableRouteList.add(newStateList);
									}
									currentState.add(next);
									// allState.add(next);
								}
								// }
							}
						}
						// }
					}
				}
			}
			step++;
			lastState.clear();
			for (int i = 0; i < currentState.size(); i++) {
				lastState.add(currentState.get(i));
			}
			currentState.clear();

			if (lastState.size() == 0) {
				break;
			}
		}

		System.out.println("Current state : " + state.getStateName());
		if (controllableRouteList.size() > 0) {
			System.out.println("Potential paths to controllability states: ");
		}
		for (int k = 0; k < controllableRouteList.size(); k++) {
			List<State> stateListTemp = controllableRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}

		if (uncontrollableRouteList.size() > 0) {
			System.out.println("Potential paths to uncontrollability states: ");
		}
		for (int k = 0; k < uncontrollableRouteList.size(); k++) {
			List<State> stateListTemp = uncontrollableRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}
		System.out.println();
	}

	private static void LookaheadWithControllabilityByDepthSearch(State state) {
		// �����ǰ��״̬Ϊ����״̬�����״̬����ֱ���������
		if (state.contain(acceptState)) {
			System.out.println("Current state : " + state.getStateName()
					+ "(Accept)");
			return;
		}
		if (state.contain(errorState)) {
			System.out.println("Current state : " + state.getStateName()
					+ "(Error)");
			return;
		}

		int step = kSteps;

		List<List<State>> controllableRouteList = new ArrayList<List<State>>();
		List<List<State>> uncontrollableRouteList = new ArrayList<List<State>>();

		List<State> stateList = new ArrayList<State>();
		stateList.add(state);
		ControllabilityDepthSearch(step, state, stateList,
				controllableRouteList, uncontrollableRouteList);

		System.out.println("Current state : " + state.getStateName());
		if (controllableRouteList.size() > 0) {
			System.out.println("Potential paths to controllability states: ");
		}
		for (int k = 0; k < controllableRouteList.size(); k++) {
			List<State> stateListTemp = controllableRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}

		if (uncontrollableRouteList.size() > 0) {
			System.out.println("Potential paths to uncontrollability states: ");
		}
		for (int k = 0; k < uncontrollableRouteList.size(); k++) {
			List<State> stateListTemp = uncontrollableRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}
		System.out.println();
	}

	private static void ControllabilityDepthSearch(int step, State state,
			List<State> stateList, List<List<State>> controllableRouteList,
			List<List<State>> uncontrollableRouteList) {
		// �����ݹ�
		if (step == 0) {
			return;
		}

		for (int j = 0; j < state.getEndStateList().size(); j++) {
			State next = state.getEndStateList().get(j);

			// ֻ���������ѭ��
			if (next.getStateName().equals(state.getStateName())) {
				continue;
			}

			if (next.contain(acceptState)) {
				List<State> newStateList = new ArrayList<State>();
				for (int i = 0; i < stateList.size(); i++) {
					newStateList.add(stateList.get(i));
				}
				newStateList.add(next);

				if (!isExist(newStateList, controllableRouteList)) {
					controllableRouteList.add(newStateList);
				}
			} else if (next.contain(errorState)) {
				List<State> newStateList = new ArrayList<State>();
				for (int i = 0; i < stateList.size(); i++) {
					newStateList.add(stateList.get(i));
				}
				newStateList.add(next);

				if (!isExist(newStateList, uncontrollableRouteList)) {
					uncontrollableRouteList.add(newStateList);
				}
			} else {
				if (next.getControllableStatus() == 0) {
					List<State> newStateList = new ArrayList<State>();
					for (int i = 0; i < stateList.size(); i++) {
						newStateList.add(stateList.get(i));
					}
					newStateList.add(next);

					if (!isExist(newStateList, uncontrollableRouteList)) {
						uncontrollableRouteList.add(newStateList);
					}

					ControllabilityDepthSearch(step - 1, next, newStateList,
							controllableRouteList, uncontrollableRouteList);
				} else if (next.getControllableStatus() == 1) {
					List<State> newStateList = new ArrayList<State>();
					for (int i = 0; i < stateList.size(); i++) {
						newStateList.add(stateList.get(i));
					}
					newStateList.add(next);

					if (!isExist(newStateList, controllableRouteList)) {
						controllableRouteList.add(newStateList);
					}

					ControllabilityDepthSearch(step - 1, next, newStateList,
							controllableRouteList, uncontrollableRouteList);
				} else {
					List<State> newStateList = new ArrayList<State>();
					for (int i = 0; i < stateList.size(); i++) {
						newStateList.add(stateList.get(i));
					}
					newStateList.add(next);
					ControllabilityDepthSearch(step - 1, next, newStateList,
							controllableRouteList, uncontrollableRouteList);
				}
			}
		}

	}

	private static boolean isExist(List<State> newStateList,
			List<List<State>> routeList) {
		boolean isExist = false;
		for (int m = 0; m < routeList.size(); m++) {
			List<State> stateListTemp = routeList.get(m);
			if (stateListTemp.size() == newStateList.size()) {
				boolean isMatch = true;
				for (int n = 0; n < stateListTemp.size(); n++) {
					if (!newStateList.get(n).getStateName()
							.equals(stateListTemp.get(n).getStateName())) {
						isMatch = false;
						break;
					}
				}

				if (isMatch) {
					isExist = true;
					break;
				}
			}
		}
		return isExist;
	}

	private static void LookaheadWithoutControllabilityByBreadthSearch(
			State state) {
		// �����ǰ��״̬Ϊ����״̬�����״̬����ֱ���������
		if (state.contain(acceptState)) {
			System.out.println("Current state : " + state.getStateName()
					+ "(Accept)");
			return;
		}
		if (state.contain(errorState)) {
			System.out.println("Current state : " + state.getStateName()
					+ "(Error)");
			return;
		}

		int step = 1;
		List<State> lastState = new ArrayList<State>();
		List<State> currentState = new ArrayList<State>();
		List<List<State>> lastRouteList = new ArrayList<List<State>>();
		List<List<State>> currentRouteList = new ArrayList<List<State>>();
		List<List<State>> acceptRouteList = new ArrayList<List<State>>();
		List<List<State>> errorRouteList = new ArrayList<List<State>>();

		lastState.add(state);
		List<State> startState = new ArrayList<State>();
		startState.add(state);
		currentState.clear();
		lastRouteList.add(startState);

		while (step <= kSteps) {
			for (int i = 0; i < lastState.size(); i++) {
				// ����ȷ��current������ֹAccept����Error״̬
				State current = lastState.get(i);

				for (int j = 0; j < current.getEndStateList().size(); j++) {
					State next = current.getEndStateList().get(j);
					// ������ѭ���ͻ�·�Ŀ��ܣ������·�Ѿ����ڵģ�����ɾ������
					for (int k = 0; k < lastRouteList.size(); k++) {
						List<State> stateListTemp = lastRouteList.get(k);
						// if( stateListTemp.size() == step)
						// {
						if (stateListTemp.get(stateListTemp.size() - 1)
								.getStateName().equals(current.getStateName())) {
							if (next.contain(acceptState)) {
								List<State> newStateList = new ArrayList<State>();
								for (int m = 0; m < stateListTemp.size(); m++) {
									newStateList.add(stateListTemp.get(m));
								}
								newStateList.add(next);
								if (!isExist(newStateList, acceptRouteList)) {
									acceptRouteList.add(newStateList);
								}
							} else if (next.contain(errorState)) {
								List<State> newStateList = new ArrayList<State>();
								for (int m = 0; m < stateListTemp.size(); m++) {
									newStateList.add(stateListTemp.get(m));
								}
								newStateList.add(next);
								if (!isExist(newStateList, errorRouteList)) {
									errorRouteList.add(newStateList);
								}
							} else {
								List<State> newStateList = new ArrayList<State>();
								for (int m = 0; m < stateListTemp.size(); m++) {
									newStateList.add(stateListTemp.get(m));
								}
								newStateList.add(next);
								if (!isExist(newStateList, currentRouteList)) {
									currentRouteList.add(newStateList);
								}
								currentState.add(next);
							}
						}
					}
				}
			}
			step++;
			lastRouteList.clear();
			for (int i = 0; i < currentRouteList.size(); i++) {
				lastRouteList.add(currentRouteList.get(i));
			}
			currentRouteList.clear();

			lastState.clear();
			for (int i = 0; i < currentState.size(); i++) {
				lastState.add(currentState.get(i));
			}
			currentState.clear();

			if (lastState.size() == 0) {
				break;
			}
		}

		System.out.println("Current state : " + state.getStateName());
		if (acceptRouteList.size() > 0) {
			System.out.println("Potential Accepting paths: ");
		}
		for (int k = 0; k < acceptRouteList.size(); k++) {
			List<State> stateListTemp = acceptRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}

		if (errorRouteList.size() > 0) {
			System.out.println("Potential Error paths: ");
		}
		for (int k = 0; k < errorRouteList.size(); k++) {
			List<State> stateListTemp = errorRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}
		System.out.println();
	}

	private static void LookaheadWithoutControllabilityByDepthSearch(State state) {
		// �����ǰ��״̬Ϊ����״̬�����״̬����ֱ���������
		if (state.contain(acceptState)) {
			System.out.println("Current state : " + state.getStateName()
					+ "(Accept)");
			return;
		}
		if (state.contain(errorState)) {
			System.out.println("Current state : " + state.getStateName()
					+ "(Error)");
			return;
		}

		int step = kSteps;

		List<List<State>> acceptRouteList = new ArrayList<List<State>>();
		List<List<State>> errorRouteList = new ArrayList<List<State>>();

		List<State> stateList = new ArrayList<State>();
		stateList.add(state);
		UncontrollabilityDepthSearch(step, state, stateList, acceptRouteList,
				errorRouteList);

		System.out.println("Current state : " + state.getStateName());
		if (acceptRouteList.size() > 0) {
			System.out.println("Potential Accepting paths: ");
		}
		for (int k = 0; k < acceptRouteList.size(); k++) {
			List<State> stateListTemp = acceptRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}

		if (errorRouteList.size() > 0) {
			System.out.println("Potential Error paths: ");
		}
		for (int k = 0; k < errorRouteList.size(); k++) {
			List<State> stateListTemp = errorRouteList.get(k);
			if (stateListTemp.size() > 1) {
				System.out.print(stateListTemp.get(stateListTemp.size() - 1)
						.getStateName()
						+ "("
						+ (stateListTemp.size() - 1)
						+ ",");
				System.out.print("(");
				for (int i = 0; i < stateListTemp.size(); i++) {
					System.out.print(stateListTemp.get(i).getStateName() + " ");
				}
				System.out.println("))");
			}
		}
		System.out.println();
	}

	private static void UncontrollabilityDepthSearch(int step, State state,
			List<State> stateList, List<List<State>> acceptRouteList,
			List<List<State>> errorRouteList) {
		// �����ݹ�
		if (step == 0) {
			return;
		}

		for (int j = 0; j < state.getEndStateList().size(); j++) {
			State next = state.getEndStateList().get(j);

			// ֻ���������ѭ��
			if (next.getStateName().equals(state.getStateName())) {
				continue;
			}

			if (next.contain(acceptState)) {
				List<State> newStateList = new ArrayList<State>();
				for (int i = 0; i < stateList.size(); i++) {
					newStateList.add(stateList.get(i));
				}
				newStateList.add(next);

				if (!isExist(newStateList, acceptRouteList)) {
					acceptRouteList.add(newStateList);
				}
			} else if (next.contain(errorState)) {
				List<State> newStateList = new ArrayList<State>();
				for (int i = 0; i < stateList.size(); i++) {
					newStateList.add(stateList.get(i));
				}
				newStateList.add(next);

				if (!isExist(newStateList, errorRouteList)) {
					errorRouteList.add(newStateList);
				}
			} else {
				List<State> newStateList = new ArrayList<State>();
				for (int i = 0; i < stateList.size(); i++) {
					newStateList.add(stateList.get(i));
				}
				newStateList.add(next);
				UncontrollabilityDepthSearch(step - 1, next, newStateList,
						acceptRouteList, errorRouteList);
			}
		}
	}

	public static boolean AnalyzingWithoutTimeRealTime() {// ������

		List<Message> messageInstances = new ArrayList<Message>();

		int falseIndex = 0;
		int trueIndex = 0;

		for (int l = 0; l < messageLog.size(); l++) {
			Message message = messageLog.get(l);
			messageInstances.add(message);
		}

		boolean TACreated = false;

		int indexI = 0;

		Message currentI = messageInstances.get(indexI);

		if (currentProcessState.getStateName().equals(
				timedAutomataState.get(0).getStateName())) {
			outer: while (currentI != null) {
				for (int i = 0; i < currentProcessState
						.getStateTimedCondition().size(); i++) {
					if (currentI.getMessageFullText()
							.equals(currentProcessState
									.getStateTimedCondition().get(i))) {
						nextProcessState = currentProcessState
								.getEndStateList().get(i);
						if (!nextProcessState.getStateName().equals(
								currentProcessState.getStateName())) {
							if (!nextProcessState.contain(errorState)) {
								sequentialObservation.add(true);
							} else {
								sequentialObservation.add(false);
								errorMessage.add(currentI.getMessageOrigin());
							}
						}
						if (kSteps > 0) {
							if (!LookAheadErrorState(currentProcessState,
									kSteps)
									|| !LookAheadAcceptState(
											currentProcessState, kSteps)) {
								System.out.println("currentMessage: "
										+ currentI.getMessageOrigin());
								System.out.println("currentState: "
										+ currentProcessState.getStateName());

								if (!LookAheadErrorState(currentProcessState,
										kSteps)) {
									System.out.print("Potential Error Paths: ");
									for (int k = 0; k < currentProcessState
											.getErrorStateInfo().size(); k++) {
										if (currentProcessState
												.getErrorStateSteps().get(k) <= kSteps
												&& currentProcessState
														.getErrorStateSteps()
														.get(k) != 0) {
											System.out
													.print(currentProcessState
															.getErrorStateInfo()
															.get(k)
															.getStateName()
															+ "("
															+ currentProcessState
																	.getErrorStateSteps()
																	.get(k)
															+ ",(");
											for (int l = currentProcessState
													.getErrorStateRoute()
													.get(k).size() - 1; l >= 0; l--) {
												System.out
														.print(currentProcessState
																.getErrorStateRoute()
																.get(k).get(l)
																.getStateName()
																+ " ");
											}
											System.out.print("))  ");
										}
									}
									System.out.println();
								}

								if (!LookAheadAcceptState(currentProcessState,
										kSteps)) {
									System.out
											.print("Potential Accepting paths: ");
									for (int k = 0; k < currentProcessState
											.getAcceptStateInfo().size(); k++) {
										if (currentProcessState
												.getAcceptStateSteps().get(k) <= kSteps
												&& currentProcessState
														.getAcceptStateSteps()
														.get(k) != 0) {
											System.out
													.print(currentProcessState
															.getAcceptStateInfo()
															.get(k)
															.getStateName()
															+ "("
															+ currentProcessState
																	.getAcceptStateSteps()
																	.get(k)
															+ ",(");
											for (int l = currentProcessState
													.getAcceptStateRoute()
													.get(k).size() - 1; l >= 0; l--) {
												System.out
														.print(currentProcessState
																.getAcceptStateRoute()
																.get(k).get(l)
																.getStateName()
																+ " ");
											}
											System.out.print("))  ");
										}
									}
									System.out.println();
								}
								System.out.println();
							}
						}
						break outer;
					}

				}

				if ((indexI + 1) < messageInstances.size())
					currentI = messageInstances.get(++indexI);
				else
					currentI = null;
			}
		}

		if (currentI != null && TACreated == false) {
			TACreated = true;
			currentI = messageInstances.get(++indexI);
			currentProcessState = nextProcessState;

			if (kSteps > 0) {
				if (!LookAheadErrorState(currentProcessState, kSteps)
						|| !LookAheadAcceptState(currentProcessState, kSteps)) {
					System.out.println("currentMessage: "
							+ currentI.getMessageOrigin());
					System.out.println("currentState: "
							+ currentProcessState.getStateName());
					if (!LookAheadErrorState(currentProcessState, kSteps)) {

						System.out.print("Potential Error Paths: ");
						for (int k = 0; k < currentProcessState
								.getErrorStateInfo().size(); k++) {
							if (currentProcessState.getErrorStateSteps().get(k) <= kSteps
									&& currentProcessState.getErrorStateSteps()
											.get(k) != 0) {
								System.out.print(currentProcessState
										.getErrorStateInfo().get(k)
										.getStateName()
										+ "("
										+ currentProcessState
												.getErrorStateSteps().get(k)
										+ ",(");
								for (int l = currentProcessState
										.getErrorStateRoute().get(k).size() - 1; l >= 0; l--) {
									System.out.print(currentProcessState
											.getErrorStateRoute().get(k).get(l)
											.getStateName()
											+ " ");
								}
								System.out.print("))  ");
							}
						}
						System.out.println();
					}
					if (!LookAheadAcceptState(currentProcessState, kSteps)) {

						System.out.print("Potential Accepting paths: ");
						for (int k = 0; k < currentProcessState
								.getAcceptStateInfo().size(); k++) {
							if (currentProcessState.getAcceptStateSteps()
									.get(k) <= kSteps
									&& currentProcessState
											.getAcceptStateSteps().get(k) != 0) {
								System.out.print(currentProcessState
										.getAcceptStateInfo().get(k)
										.getStateName()
										+ "("
										+ currentProcessState
												.getAcceptStateSteps().get(k)
										+ ",(");
								for (int l = currentProcessState
										.getAcceptStateRoute().get(k).size() - 1; l >= 0; l--) {
									System.out.print(currentProcessState
											.getAcceptStateRoute().get(k)
											.get(l).getStateName()
											+ " ");
								}
								System.out.print("))  ");
							}
						}
						System.out.println();
					}
					System.out.println();
				}
			}
			while (currentI != null) {
				outer2: for (int i = 0; i < currentProcessState
						.getStateTimedCondition().size(); i++) {

					if (currentI.getMessageFullText()
							.equals(currentProcessState
									.getStateTimedCondition().get(i))) {
						nextProcessState = currentProcessState
								.getEndStateList().get(i);
						if (!nextProcessState.getStateName().equals(
								currentProcessState.getStateName())) {
							if (!nextProcessState.contain(errorState)) {
								sequentialObservation.add(true);
							} else {
								sequentialObservation.add(false);
								errorMessage.add(currentI.getMessageOrigin());
							}
						}
						break outer2;
					}
				}

				currentProcessState = nextProcessState;
				if (kSteps > 0) {
					if (!LookAheadErrorState(currentProcessState, kSteps)
							|| !LookAheadAcceptState(currentProcessState,
									kSteps)) {
						System.out.println("currentMessage: "
								+ currentI.getMessageOrigin());
						System.out.println("currentState: "
								+ currentProcessState.getStateName());

						if (!LookAheadErrorState(currentProcessState, kSteps)) {

							System.out.print("Potential Error Paths: ");
							for (int k = 0; k < currentProcessState
									.getErrorStateInfo().size(); k++) {
								if (currentProcessState.getErrorStateSteps()
										.get(k) <= kSteps
										&& currentProcessState
												.getErrorStateSteps().get(k) != 0) {
									System.out.print(currentProcessState
											.getErrorStateInfo().get(k)
											.getStateName()
											+ "("
											+ currentProcessState
													.getErrorStateSteps()
													.get(k) + ",(");
									for (int l = currentProcessState
											.getErrorStateRoute().get(k).size() - 1; l >= 0; l--) {
										System.out.print(currentProcessState
												.getErrorStateRoute().get(k)
												.get(l).getStateName()
												+ " ");
									}
									System.out.print("))  ");
								}
							}
							System.out.println();
						}

						if (!LookAheadAcceptState(currentProcessState, kSteps)) {
							System.out.print("Potential Accepting paths: ");
							for (int k = 0; k < currentProcessState
									.getAcceptStateInfo().size(); k++) {
								if (currentProcessState.getAcceptStateSteps()
										.get(k) <= kSteps
										&& currentProcessState
												.getAcceptStateSteps().get(k) != 0) {
									System.out.print(currentProcessState
											.getAcceptStateInfo().get(k)
											.getStateName()
											+ "("
											+ currentProcessState
													.getAcceptStateSteps().get(
															k) + ",(");
									for (int l = currentProcessState
											.getAcceptStateRoute().get(k)
											.size() - 1; l >= 0; l--) {
										System.out.print(currentProcessState
												.getAcceptStateRoute().get(k)
												.get(l).getStateName()
												+ " ");
									}
									System.out.print("))  ");
								}
							}
							System.out.println();
						}
						System.out.println();
					}
				}

				if (currentProcessState.contain(acceptState)) {
					acceptMessage.add(indexI);
					if ((indexI + 1) < messageInstances.size()) {

						trueIndex++;
						currentProcessState = timedAutomataState.get(0);

						outer3: while (currentI != null) {
							for (int k = 0; k < currentProcessState
									.getStateTimedCondition().size(); k++) {
								if (currentI.getMessageFullText().equals(
										currentProcessState
												.getStateTimedCondition()
												.get(k))) {
									nextProcessState = currentProcessState
											.getEndStateList().get(k);

									break outer3;
								}
							}
							if ((indexI + 1) < messageInstances.size())
								currentI = messageInstances.get(++indexI);
							else
								currentI = null;
						}

						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
						// currentProcessState = nextProcessState;

					} else
						currentI = null;
				} else if (currentProcessState.contain(errorState)) {
					falseIndex++;
					currentProcessState = timedAutomataState.get(0);
					currentI = messageInstances.get(++indexI);

					outer3: while (currentI != null) {
						for (int k = 0; k < currentProcessState
								.getStateTimedCondition().size(); k++) {
							if (currentI.getMessageFullText().equals(
									currentProcessState
											.getStateTimedCondition().get(k))) {

								nextProcessState = currentProcessState
										.getEndStateList().get(k);

								break outer3;
							}
						}
						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
					}

					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
					// currentProcessState = nextProcessState;

				} else {
					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
				}
			}
			TACreated = false;
		}

		// System.out.println();
		// System.out.println("The number of accept messages is: "
		// + acceptMessage.size());
		// // for (int i = 0; i < errorMessage.size(); i++) {
		// // System.out.println(errorMessage.get(i));
		// // }
		//
		// System.out.println();
		// System.out.println("The number of error messages is: "
		// + errorMessage.size());
		// for (int i = 0; i < errorMessage.size(); i++) {
		// System.out.println(errorMessage.get(i));
		// }
		// System.out.println();
		return true;
	}

	public static boolean AnalyzingWithoutTime(int steps) {// ��ӵ�����

		List<Message> messageInstances = new ArrayList<Message>();

		int falseIndex = 0;
		int trueIndex = 0;

		for (int l = 0; l < messageLog.size(); l++) {
			Message message = messageLog.get(l);
			messageInstances.add(message);

		}

		boolean TACreated = false;

		int indexI = 0, indexState = 0;

		Message currentI = messageInstances.get(indexI);

		State currentState = timedAutomataState.get(indexState), nextState = null;

		List<String> errorMessage = new ArrayList<String>();
		List<Integer> acceptMessage = new ArrayList<Integer>();

		outer: while (currentI != null) {
			for (int i = 0; i < currentState.getStateTimedCondition().size(); i++) {
				if (currentI.getMessageFullText().equals(
						currentState.getStateTimedCondition().get(i))) {
					nextState = currentState.getEndStateList().get(i);
					if (!nextState.getStateName().equals(
							currentState.getStateName())) {
						if (!nextState.contain(errorState)) {
							sequentialObservation.add(true);
						} else {
							sequentialObservation.add(false);
							errorMessage.add(currentI.getMessageOrigin());
						}
					}
					if (!LookAheadErrorState(currentState, steps)) {
						System.out.println("currentMessage: "
								+ currentI.getMessageOrigin());
						System.out.println("currentState: "
								+ currentState.getStateName());

						System.out.print("Potential Error Paths: ");
						for (int k = 0; k < currentState.getErrorStateInfo()
								.size(); k++) {
							if (currentState.getErrorStateSteps().get(k) <= steps
									&& currentState.getErrorStateSteps().get(k) != 0) {
								System.out.print(currentState
										.getErrorStateInfo().get(k)
										.getStateName()
										+ "("
										+ currentState.getErrorStateSteps()
												.get(k) + ",(");
								for (int l = currentState.getErrorStateRoute()
										.get(k).size() - 1; l >= 0; l--) {
									System.out.print(currentState
											.getErrorStateRoute().get(k).get(l)
											.getStateName()
											+ " ");
								}
								System.out.print("))  ");
							}
						}
						System.out.println();

						System.out.print("Potential Accepting paths: ");
						for (int k = 0; k < currentState.getAcceptStateInfo()
								.size(); k++) {
							if (currentState.getAcceptStateSteps().get(k) <= steps
									&& currentState.getAcceptStateSteps()
											.get(k) != 0) {
								System.out.print(currentState
										.getAcceptStateInfo().get(k)
										.getStateName()
										+ "("
										+ currentState.getAcceptStateSteps()
												.get(k) + ",(");
								for (int l = currentState.getAcceptStateRoute()
										.get(k).size() - 1; l >= 0; l--) {
									System.out.print(currentState
											.getAcceptStateRoute().get(k)
											.get(l).getStateName()
											+ " ");
								}
								System.out.print("))  ");
							}
						}
						System.out.println();
						System.out.println();
					}
					break outer;
				}

			}

			if ((indexI + 1) < messageInstances.size())
				currentI = messageInstances.get(++indexI);
			else
				currentI = null;
		}

		if (currentI != null && TACreated == false) {
			TACreated = true;
			currentI = messageInstances.get(++indexI);
			currentState = nextState;

			if (!LookAheadErrorState(currentState, steps)) {
				System.out.println("currentMessage: "
						+ currentI.getMessageOrigin());
				System.out.println("currentState: "
						+ currentState.getStateName());

				System.out.print("Potential Error Paths: ");
				for (int k = 0; k < currentState.getErrorStateInfo().size(); k++) {
					if (currentState.getErrorStateSteps().get(k) <= steps
							&& currentState.getErrorStateSteps().get(k) != 0) {
						System.out.print(currentState.getErrorStateInfo()
								.get(k).getStateName()
								+ "("
								+ currentState.getErrorStateSteps().get(k)
								+ ",(");
						for (int l = currentState.getErrorStateRoute().get(k)
								.size() - 1; l >= 0; l--) {
							System.out.print(currentState.getErrorStateRoute()
									.get(k).get(l).getStateName()
									+ " ");
						}
						System.out.print("))  ");
					}
				}
				System.out.println();

				System.out.print("Potential Accepting paths: ");
				for (int k = 0; k < currentState.getAcceptStateInfo().size(); k++) {
					if (currentState.getAcceptStateSteps().get(k) <= steps
							&& currentState.getAcceptStateSteps().get(k) != 0) {
						System.out.print(currentState.getAcceptStateInfo()
								.get(k).getStateName()
								+ "("
								+ currentState.getAcceptStateSteps().get(k)
								+ ",(");
						for (int l = currentState.getAcceptStateRoute().get(k)
								.size() - 1; l >= 0; l--) {
							System.out.print(currentState.getAcceptStateRoute()
									.get(k).get(l).getStateName()
									+ " ");
						}
						System.out.print("))  ");
					}
				}
				System.out.println();
				System.out.println();
			}

			while (currentI != null) {
				outer2: for (int i = 0; i < currentState
						.getStateTimedCondition().size(); i++) {

					if (currentI.getMessageFullText().equals(
							currentState.getStateTimedCondition().get(i))) {
						nextState = currentState.getEndStateList().get(i);
						if (!nextState.getStateName().equals(
								currentState.getStateName())) {
							if (!nextState.contain(errorState)) {
								sequentialObservation.add(true);
							} else {
								sequentialObservation.add(false);
								errorMessage.add(currentI.getMessageOrigin());
							}
						}
						break outer2;
					}
				}

				currentState = nextState;
				if (!LookAheadErrorState(currentState, steps)) {
					System.out.println("currentMessage: "
							+ currentI.getMessageOrigin());
					System.out.println("currentState: "
							+ currentState.getStateName());

					System.out.print("Potential Error Paths: ");
					for (int k = 0; k < currentState.getErrorStateInfo().size(); k++) {
						if (currentState.getErrorStateSteps().get(k) <= steps
								&& currentState.getErrorStateSteps().get(k) != 0) {
							System.out.print(currentState.getErrorStateInfo()
									.get(k).getStateName()
									+ "("
									+ currentState.getErrorStateSteps().get(k)
									+ ",(");
							for (int l = currentState.getErrorStateRoute()
									.get(k).size() - 1; l >= 0; l--) {
								System.out.print(currentState
										.getErrorStateRoute().get(k).get(l)
										.getStateName()
										+ " ");
							}
							System.out.print("))  ");
						}
					}
					System.out.println();

					System.out.print("Potential Accepting paths: ");
					for (int k = 0; k < currentState.getAcceptStateInfo()
							.size(); k++) {
						if (currentState.getAcceptStateSteps().get(k) <= steps
								&& currentState.getAcceptStateSteps().get(k) != 0) {
							System.out.print(currentState.getAcceptStateInfo()
									.get(k).getStateName()
									+ "("
									+ currentState.getAcceptStateSteps().get(k)
									+ ",(");
							for (int l = currentState.getAcceptStateRoute()
									.get(k).size() - 1; l >= 0; l--) {
								System.out.print(currentState
										.getAcceptStateRoute().get(k).get(l)
										.getStateName()
										+ " ");
							}
							System.out.print("))  ");
						}
					}
					System.out.println();
					System.out.println();
				}

				if (currentState.contain(acceptState)) {
					acceptMessage.add(indexI);
					if ((indexI + 1) < messageInstances.size()) {

						trueIndex++;
						currentState = timedAutomataState.get(0);

						outer3: while (currentI != null) {
							for (int k = 0; k < currentState
									.getStateTimedCondition().size(); k++) {
								if (currentI.getMessageFullText().equals(
										currentState.getStateTimedCondition()
												.get(k))) {
									nextState = currentState.getEndStateList()
											.get(k);

									break outer3;
								}
							}
							if ((indexI + 1) < messageInstances.size())
								currentI = messageInstances.get(++indexI);
							else
								currentI = null;
						}

						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
						currentState = nextState;

					} else
						currentI = null;
				} else if (currentState.contain(errorState)) {
					falseIndex++;
					currentState = timedAutomataState.get(0);
					currentI = messageInstances.get(++indexI);

					outer3: while (currentI != null) {
						for (int k = 0; k < currentState
								.getStateTimedCondition().size(); k++) {
							if (currentI.getMessageFullText().equals(
									currentState.getStateTimedCondition()
											.get(k))) {

								nextState = currentState.getEndStateList().get(
										k);

								break outer3;
							}
						}
						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
					}

					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
					currentState = nextState;

				} else {
					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
				}
			}
			TACreated = false;
		}

		return true;
	}

	public static boolean AnalyzingWithTime() {

		List<Message> messageInstances = new ArrayList<Message>();

		List<Double> timeInstances = new ArrayList<Double>();

		// System.out.println("The Analyzer is initialized: state in s0;");

		String firstTime = "";
		if (!messageLog.isEmpty()) {
			firstTime = messageLog.get(0).getTimedCondition();
		}

		int falseIndex = 0;
		int trueIndex = 0;

		for (int l = 0; l < messageLog.size(); l++) {
			Message message = messageLog.get(l);

			message.setTime(firstTime);
			// System.out.println(message.getTime());
			messageInstances.add(message);
			timeInstances.add(message.getTime());

		}

		// for ( int i = 0 ; i < timeInstances.size(); i++)
		// {
		// System.out.println(timeInstances.get(i));
		// }

		double xTimeInitialize = 0.0;

		double yTimeInitialize = 0.0;

		boolean TACreated = false;

		int indexI = 0, indexState = 0;

		Message currentI = messageInstances.get(indexI);

		State currentState = timedAutomataState.get(indexState), nextState = null;

		@SuppressWarnings("unused")
		double currentTime = 0.0;

		errorMessage.clear();
		acceptMessage.clear();

		xTimeInitialize = timeInstances.get(0);

		outer: while (currentI != null) {
			for (int i = 0; i < currentState.getStateTimedCondition().size(); i++) {
				if (currentI.getMessageFullText().equals(
						currentState.getStateTimedCondition().get(i))) {
					nextState = currentState.getEndStateList().get(i);
					if (!nextState.getStateName().equals(
							currentState.getStateName())) {
						xTimeInitialize = timeInstances.get(indexI);
						// System.out.println(currentI.getMessageOrigin());
						if (!nextState.contain(errorState)) {
							// System.out.println("1");
							sequentialObservation.add(true);
							// System.out.println("     "
							// + "Transition found, from "
							// + currentState.getStateName() + " to "
							// + nextState.getStateName() + " at time = "
							// + (currentI.getTime() - currentTime)
							// + "second");
						} else {
							// System.out.println("0");
							sequentialObservation.add(false);
							// System.out.println("     "
							// + "Transition found, from "
							// + currentState.getStateName() + " to "
							// + nextState.getStateName()
							// + "(Error State)" + " at time = "
							// + (currentI.getTime() - currentTime)
							// + "second");
							// System.out.println("       ***Violation***;");
							errorMessage.add(currentI.getMessageOrigin());
						}
						currentTime = currentI.getTime();
					}
					break outer;
				}

			}
			if ((indexI + 1) < messageInstances.size())
				currentI = messageInstances.get(++indexI);
			else
				currentI = null;
		}

		if (currentI != null && TACreated == false) {
			TACreated = true;
			currentI = messageInstances.get(++indexI);
			currentState = nextState;
			// System.out.println(currentState.getStateName());
			while (currentI != null) {
				outer2: for (int i = 0; i < currentState
						.getStateTimedCondition().size(); i++) {

					if (currentState.getXSymbol().get(i) == 3) {
						// xTimeInitialize = timeInstances.get(indexI);
						// System.out.println("--------="+indexI);
						if (currentI.getMessageFullText().equals(
								currentState.getStateTimedCondition().get(i))) {
							nextState = currentState.getEndStateList().get(i);
							if (!nextState.getStateName().equals(
									currentState.getStateName())) {
								// System.out.println(currentI.getMessageOrigin());
								if (!nextState.contain(errorState)) {
									// System.out.println("1");
									sequentialObservation.add(true);
									// System.out
									// .println("     "
									// + "Transition found, from "
									// + currentState
									// .getStateName()
									// + " to "
									// + nextState.getStateName()
									// + " at time = "
									// + (currentI.getTime() - currentTime)
									// + "min");
								} else {
									// System.out.println("0");
									sequentialObservation.add(false);
									// System.out
									// .println("     "
									// + "Transition found, from "
									// + currentState
									// .getStateName()
									// + " to "
									// + nextState.getStateName()
									// + "(Error State)"
									// + " at time = "
									// + (currentI.getTime() - currentTime)
									// + "min");
									// System.out
									// .println("       ***Violation***;");
									errorMessage.add(currentI
											.getMessageOrigin());
								}
								currentTime = currentI.getTime();
							}

						}
						if (currentState.getYSymbol().get(i) == -1) {
							break outer2;
						}
					} else {
						if (currentState.getXSymbol().get(i) == 0
								|| currentState.getXSymbol().get(i) == 1) {
							// System.out.println((timeInstances.get(indexI) -
							// xTimeInitialize)+ " " +
							// currentState.getX().get(i));
							if ((timeInstances.get(indexI) - xTimeInitialize) <= (currentState
									.getX().get(i))) {
								if (currentState.getYesORnoList().get(i)) {
									if (currentI.getMessageFullText().equals(
											currentState
													.getStateTimedCondition()
													.get(i))) {
										nextState = currentState
												.getEndStateList().get(i);
										if (!nextState.getStateName().equals(
												currentState.getStateName())) {
											// System.out.println(currentI
											// .getMessageOrigin());
											if (!nextState.contain(errorState)) {
												// System.out.println("--------<"+indexI);
												// System.out.println("1");
												sequentialObservation.add(true);
												// System.out
												// .println("     "
												// + "Transition found, from "
												// + currentState
												// .getStateName()
												// + " to "
												// + nextState
												// .getStateName()
												// + " at time = "
												// + (currentI
												// .getTime() - currentTime)
												// + "min");
											} else {
												// System.out.println("0");
												sequentialObservation
														.add(false);
												// System.out
												// .println("     "
												// + "Transition found, from "
												// + currentState
												// .getStateName()
												// + " to "
												// + nextState
												// .getStateName()
												// + "(Error State)"
												// + " at time = "
												// + (currentI
												// .getTime() - currentTime)
												// + "min");
												// System.out
												// .println("       ***Violation***;");
												errorMessage.add(currentI
														.getMessageOrigin());
											}
											currentTime = currentI.getTime();
										}
										if (currentState.getYSymbol().get(i) == -1) {
											break outer2;
										} else if (currentState.getYSymbol()
												.get(i) == 3) {
											yTimeInitialize = timeInstances
													.get(indexI);
											break outer2;
										}
									}
								} else {
									if (!currentI.getMessageFullText().equals(
											currentState
													.getStateTimedCondition()
													.get(i))) {
										nextState = currentState
												.getEndStateList().get(i);
										if (!nextState.getStateName().equals(
												currentState.getStateName())) {
											// System.out.println(currentI
											// .getMessageOrigin());
											if (!nextState.contain(errorState)) {
												// System.out.println("1");
												sequentialObservation.add(true);
												// System.out
												// .println("     "
												// + "Transition found, from "
												// + currentState
												// .getStateName()
												// + " to "
												// + nextState
												// .getStateName()
												// + " at time = "
												// + (currentI
												// .getTime() - currentTime)
												// + "min");
											} else {
												// System.out.println("0");
												sequentialObservation.add(true);
												// System.out
												// .println("     "
												// + "Transition found, from "
												// + currentState
												// .getStateName()
												// + " to "
												// + nextState
												// .getStateName()
												// + "(Error State)"
												// + " at time = "
												// + (currentI
												// .getTime() - currentTime)
												// + "min");
												// System.out
												// .println("       ***Violation***;");
												errorMessage.add(currentI
														.getMessageOrigin());
											}
											currentTime = currentI.getTime();
										}
										if (currentState.getYSymbol().get(i) == -1) {
											break outer2;
										} else if (currentState.getYSymbol()
												.get(i) == 3) {
											yTimeInitialize = timeInstances
													.get(indexI);
											break outer2;
										}
									}
								}
							}
						} else if (currentState.getXSymbol().get(i) == 2) {
							if ((timeInstances.get(indexI) - xTimeInitialize) > (currentState
									.getX().get(i))) {
								nextState = currentState.getEndStateList().get(
										i);
								if (!nextState.getStateName().equals(
										currentState.getStateName())) {
									// System.out.println(currentI
									// .getMessageOrigin());
									if (!nextState.contain(errorState)) {
										// System.out.println("1");
										sequentialObservation.add(true);
										// System.out
										// .println("     "
										// + "Transition found, from "
										// + currentState
										// .getStateName()
										// + " to "
										// + nextState
										// .getStateName()
										// + " at time = "
										// + (currentI.getTime() - currentTime)
										// + "min");
									} else {
										// System.out.println("0");
										sequentialObservation.add(false);
										// System.out
										// .println("     "
										// + "Transition found, from "
										// + currentState
										// .getStateName()
										// + " to "
										// + nextState
										// .getStateName()
										// + "(Error State)"
										// + " at time = "
										// + (currentI.getTime() - currentTime)
										// + "min");
										// System.out
										// .println("       ***Violation***;");
										errorMessage.add(currentI
												.getMessageOrigin());
									}
									currentTime = currentI.getTime();
								}
								if (currentState.getYSymbol().get(i) == -1) {
									break outer2;
								} else if (currentState.getYSymbol().get(i) == 3) {
									yTimeInitialize = timeInstances.get(indexI);
									break outer2;
								}
							}

						}
					}

					if (currentState.getYSymbol().get(i) == 0
							|| currentState.getYSymbol().get(i) == 1) {
						if ((timeInstances.get(indexI) - yTimeInitialize) <= (currentState
								.getY().get(i))) {
							if (currentState.getYesORnoList().get(i)) {
								if (currentI.getMessageFullText().equals(
										currentState.getStateTimedCondition()
												.get(i))) {
									nextState = currentState.getEndStateList()
											.get(i);
									if (!nextState.getStateName().equals(
											currentState.getStateName())) {
										// System.out.println(currentI
										// .getMessageOrigin());
										if (!nextState.contain(errorState)) {
											// System.out.println("1");
											sequentialObservation.add(true);
											// System.out
											// .println("     "
											// + "Transition found, from "
											// + currentState
											// .getStateName()
											// + " to "
											// + nextState
											// .getStateName()
											// + " at time = "
											// + (currentI
											// .getTime() - currentTime)
											// + "min");
										} else {
											// System.out.println("0");
											sequentialObservation.add(false);
											// System.out
											// .println("     "
											// + "Transition found, from "
											// + currentState
											// .getStateName()
											// + " to "
											// + nextState
											// .getStateName()
											// + "(Error State)"
											// + " at time = "
											// + (currentI
											// .getTime() - currentTime)
											// + "min");
											// System.out
											// .println("       ***Violation***;");
											errorMessage.add(currentI
													.getMessageOrigin());
										}
										currentTime = currentI.getTime();
									}
									break outer2;
								}
							} else {
								if (!currentI.getMessageFullText().equals(
										currentState.getStateTimedCondition()
												.get(i))) {
									nextState = currentState.getEndStateList()
											.get(i);
									if (!nextState.getStateName().equals(
											currentState.getStateName())) {
										// System.out.println(currentI
										// .getMessageOrigin());
										if (!nextState.contain(errorState)) {
											// System.out.println("1");
											sequentialObservation.add(true);
											// System.out
											// .println("     "
											// + "Transition found, from "
											// + currentState
											// .getStateName()
											// + " to "
											// + nextState
											// .getStateName()
											// + " at time = "
											// + (currentI
											// .getTime() - currentTime)
											// + "min");
										} else {
											// System.out.println("0");
											sequentialObservation.add(false);
											// System.out
											// .println("     "
											// + "Transition found, from "
											// + currentState
											// .getStateName()
											// + " to "
											// + nextState
											// .getStateName()
											// + "(Error State)"
											// + " at time = "
											// + (currentI
											// .getTime() - currentTime)
											// + "min");
											// System.out
											// .println("       ***Violation***;");
											errorMessage.add(currentI
													.getMessageOrigin());
										}
										currentTime = currentI.getTime();
									}
									break outer2;
								}
							}
						}
					} else if (currentState.getYSymbol().get(i) == 2) {
						if ((timeInstances.get(indexI) - yTimeInitialize) > (currentState
								.getY().get(i))) {
							nextState = currentState.getEndStateList().get(i);
							if (!nextState.getStateName().equals(
									currentState.getStateName())) {
								// System.out.println(currentI.getMessageOrigin());
								if (!nextState.contain(errorState)) {
									// System.out.println("1");
									sequentialObservation.add(true);
									// System.out
									// .println("     "
									// + "Transition found, from "
									// + currentState
									// .getStateName()
									// + " to "
									// + nextState.getStateName()
									// + " at time = "
									// + (currentI.getTime() - currentTime)
									// + "min");
								} else {
									// System.out.println("0");
									sequentialObservation.add(false);
									// System.out
									// .println("     "
									// + "Transition found, from "
									// + currentState
									// .getStateName()
									// + " to "
									// + nextState.getStateName()
									// + "(Error State)"
									// + " at time = "
									// + (currentI.getTime() - currentTime)
									// + "min");
									// System.out
									// .println("       ***Violation***;");
									errorMessage.add(currentI
											.getMessageOrigin());
								}
								currentTime = currentI.getTime();
							}
							break outer2;

						}
					}
					// }
				}

				// System.out.println(currentI.getMessageOrigin());
				//
				// System.out.println(currentState.getStateName()+" "+nextState.getStateName());

				// for(int i = 0 ; i< errorState.size();i++){
				// System.out.println(errorState.size()+errorState.get(i).getStateName());
				// }

				currentState = nextState;

				if (currentState.contain(acceptState)) {
					// System.out.println("accept");
					acceptMessage.add(indexI);
					if ((indexI + 1) < messageInstances.size()) {

						trueIndex++;
						currentState = timedAutomataState.get(0);
						// xTimeInitialize = timeInstances.get(indexI);
						// yTimeInitialize = timeInstances.get(indexI);

						outer3: while (currentI != null) {
							for (int k = 0; k < currentState
									.getStateTimedCondition().size(); k++) {
								if (currentI.getMessageFullText().equals(
										currentState.getStateTimedCondition()
												.get(k))) {
									xTimeInitialize = timeInstances.get(indexI);
									yTimeInitialize = timeInstances.get(indexI);
									nextState = currentState.getEndStateList()
											.get(k);
									// if(!nextState.getStateName().equals(currentState.getStateName())){
									// System.out.println(currentI.getMessageOrigin());
									// System.out.println("     "+"Transition found, from "+currentState.getStateName()+" to "+(nextState.contain(errorState)?(nextState.getStateName()+"(Error State)"):nextState.getStateName())+" at time = "+(currentI.getTime()-currentTime)+"min");
									// currentTime = currentI.getTime();
									// }

									break outer3;
								}
							}
							if ((indexI + 1) < messageInstances.size())
								currentI = messageInstances.get(++indexI);
							else
								currentI = null;
						}

						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
						currentState = nextState;

					} else
						currentI = null;
				} else if (currentState.contain(errorState)) {
					// System.out.println("0");
					falseIndex++;
					currentState = timedAutomataState.get(0);
					currentI = messageInstances.get(++indexI);

					// currentState = timedAutomataState.get(0);
					// xTimeInitialize = timeInstances.get(indexI);
					// yTimeInitialize = timeInstances.get(indexI);

					outer3: while (currentI != null) {
						for (int k = 0; k < currentState
								.getStateTimedCondition().size(); k++) {
							if (currentI.getMessageFullText().equals(
									currentState.getStateTimedCondition()
											.get(k))) {
								xTimeInitialize = timeInstances.get(indexI);
								yTimeInitialize = timeInstances.get(indexI);

								nextState = currentState.getEndStateList().get(
										k);
								// if(!nextState.getStateName().equals(currentState.getStateName())){
								// System.out.println(currentI.getMessageOrigin());
								// System.out.println("     "+"Transition found, from "+currentState.getStateName()+" to "+(nextState.contain(errorState)?(nextState.getStateName()+"(Error State)"):nextState.getStateName())+" at time = "+(currentI.getTime()-currentTime)+"min");
								// currentTime = currentI.getTime();
								// }

								break outer3;
							}
						}
						if ((indexI + 1) < messageInstances.size())
							currentI = messageInstances.get(++indexI);
						else
							currentI = null;
					}

					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
					currentState = nextState;

				} else {
					if ((indexI + 1) < messageInstances.size())
						currentI = messageInstances.get(++indexI);
					else
						currentI = null;
				}
			}
			TACreated = false;
		}

		System.out.println();
		System.out.println("The number of accept messages is: "
				+ acceptMessage.size());
		// for (int i = 0; i < errorMessage.size(); i++) {
		// System.out.println(errorMessage.get(i));
		// }

		System.out.println();
		System.out.println("The number of error messages is: "
				+ errorMessage.size());
		for (int i = 0; i < errorMessage.size(); i++) {
			System.out.println(errorMessage.get(i));
		}

		return true;
	}

	public static Boolean GetTimedAutomataStateWithMultiAutomata() {// ��initial��ͷ�ķ�������
	// List<State> timedAutomataState = new ArrayList<State>();//tjf

		for (int no = 0; no < IImageKeys.automataFilesName.size(); no++) {
			TimedAutomataSet timedAutomataSet = timedAutomataSetList.get(no);
			List<State> timedAutomataState = new ArrayList<State>();
			List<State> acceptState = new ArrayList<State>();
			List<State> errorState = new ArrayList<State>();

			// ��һ�Σ�������ʼ״̬
			// ����һ���Զ����ļ���������Ŀ����,�õ�һ������������ʼ״̬�Ϳɽ�����Ϣ��������timedAutomataState
			for (int i = 0; i < timedAutomataSet.getTimedAutomata().size(); i++) { //billy�������size��ָtimeset�����ܹ��ж�������Ϣ��
				TimedAutomata ta = timedAutomataSet.getTimedAutomata().get(i);//billy���õ��Զ����ļ��е�һ��
				String startStateString = ta.getStartStatus();

				// if(i==0)
				// initStateNameList.add(ta.getStartStatus());//��ÿ���Զ�������ʼ״̬�����ƴ������������������п�����Ϣ��ϵ�ʱ����

				boolean taIntimedAutomataState = false;
				for (int j = 0; j < timedAutomataState.size(); j++) { //billy��һ���Զ����з�֧��ʱ��������
					if (startStateString.equals(timedAutomataState.get(j)
							.getStateName())) {
					//	System.out.println(timedAutomataState.get(j).getStateName()+" ��ʼ��2 "+ta.getAutomataMessage());
						taIntimedAutomataState = true;
						timedAutomataState.get(j).addStateTimedCondition(
								ta.getAutomataMessage(), ta.getMessageType());
						timedAutomataState.get(j)
								.addYesORnoList(ta.isYesORno());
						timedAutomataState.get(j).addX(ta.getX());
						timedAutomataState.get(j).addXSymbol(ta.getXSymbol());
						timedAutomataState.get(j).addY(ta.getY());
						timedAutomataState.get(j).addYSymbol(ta.getYSymbol());
						timedAutomataState.get(j).setEndStateMap(
								ta.getAutomataMessage(), ta.getEndStatus());// ����hashӳ��
																			// tjf
																			// 20110922
					}
				}
				if (taIntimedAutomataState == false) {
					State newState = new State();
				//	System.out.println(startStateString+" ��ʼ��1 "+ta.getAutomataMessage());
					newState.setStateName(startStateString);
					newState.addStateTimedCondition(ta.getAutomataMessage(),
							ta.getMessageType());
					newState.addYesORnoList(ta.isYesORno());
					newState.addX(ta.getX());
					newState.addXSymbol(ta.getXSymbol());
					newState.addY(ta.getY());
					newState.addYSymbol(ta.getYSymbol());
					newState.setEndStateMap(ta.getAutomataMessage(),
							ta.getEndStatus());// ����hashӳ�� tjf 20110922
					timedAutomataState.add(newState);
				}

			}

			// �ڶ��α�����ֹ״̬
			for (int i = 0; i < timedAutomataSet.getTimedAutomata().size(); i++) {
				TimedAutomata ta = timedAutomataSet.getTimedAutomata().get(i);
				String endStateString = ta.getEndStatus();

				String otherString = null;

				State endState = null;

				if (endStateString.contains("(")) {
					otherString = endStateString.substring(
							endStateString.indexOf("(") + 1,
							endStateString.indexOf(")"));
					endStateString = endStateString.substring(0,
							endStateString.indexOf("("));
			//		 System.out.println(endStateString);
				} else {
					otherString = null;
				}

				boolean endInStateList = false;
				for (int j = 0; j < timedAutomataState.size(); j++) {
					if (endStateString.equals(timedAutomataState.get(j)
							.getStateName())) {
						endInStateList = true;
					}
				}

				if (endInStateList == false) {
					endState = new State();
					endState.setStateName(endStateString);
					timedAutomataState.add(endState);
					if (otherString != null) {
						if (otherString.equals("Accept")) {
							acceptState.add(endState);
						} else if (otherString.equals("Error")) {
							errorState.add(endState);
						}
					}
				}
			}

			// ��ӳ�ʼ״̬ӳ����ֹ״̬�ļ���
			for (int i = 0; i < timedAutomataSet.getTimedAutomata().size(); i++) {
				TimedAutomata ta = timedAutomataSet.getTimedAutomata().get(i);
				String startStateString = ta.getStartStatus();

				String endStateString = ta.getEndStatus();

				if (endStateString.contains("(")) {
					endStateString = endStateString.substring(0,
							endStateString.indexOf("("));
					// System.out.println(endStateString);
				}

				State startState = null, endState = null;

				// System.out.println(timedAutomataState.size());
				for (int j = 0; j < timedAutomataState.size(); j++) {

					if (startStateString.equals(timedAutomataState.get(j)
							.getStateName())) {
						startState = timedAutomataState.get(j);
					}
					if (endStateString.equals(timedAutomataState.get(j)
							.getStateName())) {
						endState = timedAutomataState.get(j);
					}

				}
				if (startState != null && endState != null) {
					startState.addEndStateList(endState);
					// System.out.println(startState.getStateName()+"
					// "+endState.getStateName());
				}
			}

			// if (timedAutomataState.size() > 0) {
			// currentProcessStateList.add(timedAutomataState.get(0));
			// nextProcessStateList.add(currentProcessStateList.get(no));
			// }tjf
		//	for(int i=0;i<timedAutomataState.size();i++){
			//	State ta = timedAutomataState.get(i);
			//	System.out.println("ta"+ta.getYesORnoList());
			//}
			
			
			boolean ispsc=true;
			for (int i = 0; i < timedAutomataSet.getTimedAutomata().size(); i++) { //billy�������size��ָtimeset�����ܹ��ж�������Ϣ��
				TimedAutomata ta = timedAutomataSet.getTimedAutomata().get(i);//billy���õ��Զ����ļ��е�һ��
			    String b="Error";
		//		System.out.println("11");
				if(ta.getEndStatus().contains(b)){
					ispsc=false;
					break;
					
				}
				
			}
			if(ispsc)
			{
				timedAutomataStateList.add(timedAutomataState);
			}
			else{
				timedAutomataStateListPSC.add(timedAutomataState);
			}
			
			
			
			
       
		}
		return true;
	}

	public static Boolean GetTimedAutomataState() {
		timedAutomataState.clear();
		acceptState.clear();
		errorState.clear();

		// ��һ�Σ�������ʼ״̬
		for (int i = 0; i < timedAutomataSet.getTimedAutomata().size(); i++) {
			TimedAutomata ta = timedAutomataSet.getTimedAutomata().get(i);
			String startStateString = ta.getStartStatus();

			boolean taIntimedAutomataState = false;
			for (int j = 0; j < timedAutomataState.size(); j++) {
				if (startStateString.equals(timedAutomataState.get(j)
						.getStateName())) {
					taIntimedAutomataState = true;
					timedAutomataState.get(j).addStateTimedCondition(
							ta.getAutomataMessage());
					timedAutomataState.get(j).addYesORnoList(ta.isYesORno());
					timedAutomataState.get(j).addX(ta.getX());
					timedAutomataState.get(j).addXSymbol(ta.getXSymbol());
					timedAutomataState.get(j).addY(ta.getY());
					timedAutomataState.get(j).addYSymbol(ta.getYSymbol());
				}
			}
			if (taIntimedAutomataState == false) {
				State newState = new State();
				newState.setStateName(startStateString);
				newState.addStateTimedCondition(ta.getAutomataMessage());
				newState.addYesORnoList(ta.isYesORno());
				newState.addX(ta.getX());
				newState.addXSymbol(ta.getXSymbol());
				newState.addY(ta.getY());
				newState.addYSymbol(ta.getYSymbol());
				timedAutomataState.add(newState);
			}

		}

		// �ڶ��α�����ֹ״̬
		for (int i = 0; i < timedAutomataSet.getTimedAutomata().size(); i++) {
			TimedAutomata ta = timedAutomataSet.getTimedAutomata().get(i);
			String endStateString = ta.getEndStatus();

			String otherString = null;

			State endState = null;

			if (endStateString.contains("(")) {
				otherString = endStateString.substring(
						endStateString.indexOf("(") + 1,
						endStateString.indexOf(")"));
				endStateString = endStateString.substring(0,
						endStateString.indexOf("("));
				// System.out.println(endStateString);
			} else {
				otherString = null;
			}

			boolean endInStateList = false;
			for (int j = 0; j < timedAutomataState.size(); j++) {
				if (endStateString.equals(timedAutomataState.get(j)
						.getStateName())) {
					endInStateList = true;
				}
			}

			if (endInStateList == false) {
				endState = new State();
				endState.setStateName(endStateString);
				timedAutomataState.add(endState);
				if (otherString != null) {
					if (otherString.equals("Accept")) {
						acceptState.add(endState);
					} else if (otherString.equals("Error")) {
						errorState.add(endState);
					}
				}
			}
		}

		// ��ӳ�ʼ״̬ӳ����ֹ״̬�ļ���
		for (int i = 0; i < timedAutomataSet.getTimedAutomata().size(); i++) {
			TimedAutomata ta = timedAutomataSet.getTimedAutomata().get(i);
			String startStateString = ta.getStartStatus();

			String endStateString = ta.getEndStatus();

			if (endStateString.contains("(")) {
				endStateString = endStateString.substring(0,
						endStateString.indexOf("("));
				// System.out.println(endStateString);
			}

			State startState = null, endState = null;

			// System.out.println(timedAutomataState.size());
			for (int j = 0; j < timedAutomataState.size(); j++) {

				if (startStateString.equals(timedAutomataState.get(j)
						.getStateName())) {
					startState = timedAutomataState.get(j);
				}
				if (endStateString.equals(timedAutomataState.get(j)
						.getStateName())) {
					endState = timedAutomataState.get(j);
				}

			}
			if (startState != null && endState != null) {
				startState.addEndStateList(endState);
				// System.out.println(startState.getStateName()+"
				// "+endState.getStateName());
			}
		}

		if (timedAutomataState.size() > 0) {
			currentProcessState = timedAutomataState.get(0);
			nextProcessState = currentProcessState;
		}

		return true;
	}

	public static Boolean SetTimedAutomataSetWithMultiAutomata(
			List<String> timedAutomataFilesName) {// ��initial��ͷ�ķ�������
		// TODO Auto-generated method stub

		for (int no = 0; no < timedAutomataFilesName.size(); no++) {
			TimedAutomataSet timedAutomataSet = new TimedAutomataSet();
			try {
				BufferedReader timedAutomataReader = new BufferedReader(
						new FileReader(timedAutomataFilesName.get(no)));

				String s = new String();
				TimedAutomata timedAutomata = null;

				while ((s = timedAutomataReader.readLine()) != null) {
					String startStatus = s.substring(s.indexOf("(") + 1,
							s.indexOf(","));// ��ʼ״̬
					s = s.substring(s.indexOf(",") + 1);
					String automataMessage = s.substring(0, s.indexOf(","));// ��Ϣ��
					
					
					

				
					// ������tjf��ӣ��ж���Ϣ���ĺ������ޱ�ʾ���͡����յķ���
					int messageType = 0;
					if (automataMessage.endsWith("!")) {
						messageType = 1;
						automataMessage = automataMessage.substring(0,
								automataMessage.length() - 1);
//						System.out.println("init1 "+messageType+"  "+automataMessage);
					} else if (automataMessage.endsWith("?")) {
						messageType = 2;
						automataMessage = automataMessage.substring(0,
								automataMessage.length() - 1);
//						System.out.println("init2 "+messageType+"  "+automataMessage);
					}
					// ������tjf��ӣ��ж���Ϣ���ĺ������ޱ�ʾ���͡����յķ���

					s = s.substring(s.indexOf(",") + 1);

					String timedCondition = s.substring(s.indexOf("(") + 1,
							s.indexOf(")"));// ʱ������

					String endStatus = s.substring(s.indexOf(",") + 1,
							s.lastIndexOf(")"));// ����״̬
					
					boolean yesORno = false;
					String b="Error";
					if (endStatus.contains(b)) {// �ж�yesorno
						yesORno = false;
					
					} else {
						yesORno = true;
					}


					timedAutomata = new TimedAutomata(startStatus, yesORno,
							automataMessage, timedCondition, endStatus,
							messageType);

					if (timedCondition.contains("x")) {
						int xIndex = timedCondition.indexOf("x");
						String symbol = timedCondition.substring(xIndex + 1,
								xIndex + 2);
						if (symbol.equals("<")) {
							timedAutomata.setXSymbol(0);
						} else if (symbol.equals("=")) {
							timedAutomata.setXSymbol(1);
						} else if (symbol.equals(">")) {
							timedAutomata.setXSymbol(2);
						} else if (symbol.equals(":")) {
							timedAutomata.setXSymbol(3);
						}

						int xIntStart = xIndex, xIntEnd;
						while (!Character.isDigit(timedCondition
								.charAt(xIntStart))) {
							xIntStart++;
						}

						int xInt = xIntStart;
						while (xInt < timedCondition.length()
								&& Character.isDigit(timedCondition
										.charAt(xInt))) {
							xInt++;
						}
						xIntEnd = xInt;

						timedAutomata.setX(Integer.parseInt(timedCondition
								.substring(xIntStart, xIntEnd)));

					}

					if (timedCondition.contains("y")) {
						int yIndex = timedCondition.indexOf("y");
						String symbol = timedCondition.substring(yIndex + 1,
								yIndex + 2);
						if (symbol.equals("<")) {
							timedAutomata.setYSymbol(0);
						} else if (symbol.equals("=")) {
							timedAutomata.setYSymbol(1);
						} else if (symbol.equals(">")) {
							timedAutomata.setYSymbol(2);
						} else if (symbol.equals(":")) {
							timedAutomata.setYSymbol(3);
						}

						int yIntStart = yIndex + 2, yIntEnd;
						while (!Character.isDigit(timedCondition
								.charAt(yIntStart))) {
							yIntStart++;
						}

						int yInt = yIntStart;
						while (yInt < timedCondition.length()
								&& Character.isDigit(timedCondition
										.charAt(yInt))) {
							yInt++;
						}
						yIntEnd = yInt;
						timedAutomata.setY(Integer.parseInt(timedCondition
								.substring(yIntStart, yIntEnd)));
					}
              //     System.out.println("�Զ���������"+timedAutomata.isYesORno());
					timedAutomataSet.addTimedAutomata(timedAutomata);// һ��set��Ӧһ���Զ����ļ�,һ��timedAutomata��ʾ�Զ����ļ���һ����Ŀ
				}
				timedAutomataReader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timedAutomataSetList.add(timedAutomataSet);
		}
		return true;
	}

	public static Boolean SetTimedAutomataSet(String timedAutomataFileName) {
		// TODO Auto-generated method stub

		timedAutomataSet.clear();
		try {
			BufferedReader timedAutomataReader = new BufferedReader(
					new FileReader(timedAutomataFileName));

			String s = new String();
			TimedAutomata timedAutomata = null;

			while ((s = timedAutomataReader.readLine()) != null) {
				String startStatus = s.substring(s.indexOf("(") + 1,
						s.indexOf(","));
				s = s.substring(s.indexOf(",") + 1);
				String automataMessage = s.substring(0, s.indexOf(","));

				boolean yesORno = false;
				if (automataMessage.startsWith("!")) {
					yesORno = false;
					automataMessage = automataMessage.substring(1);
				} else {
					yesORno = true;
				}
				s = s.substring(s.indexOf(",") + 1);

				String timedCondition = s.substring(s.indexOf("(") + 1,
						s.indexOf(")"));

				String endStatus = s.substring(s.indexOf(",") + 1,
						s.lastIndexOf(")"));

				timedAutomata = new TimedAutomata(startStatus, yesORno,
						automataMessage, timedCondition, endStatus);

				if (timedCondition.contains("x")) {
					int xIndex = timedCondition.indexOf("x");
					String symbol = timedCondition.substring(xIndex + 1,
							xIndex + 2);
					if (symbol.equals("<")) {
						timedAutomata.setXSymbol(0);
					} else if (symbol.equals("=")) {
						timedAutomata.setXSymbol(1);
					} else if (symbol.equals(">")) {
						timedAutomata.setXSymbol(2);
					} else if (symbol.equals(":")) {
						timedAutomata.setXSymbol(3);
					}

					int xIntStart = xIndex, xIntEnd;
					while (!Character.isDigit(timedCondition.charAt(xIntStart))) {
						xIntStart++;
					}

					int xInt = xIntStart;
					while (xInt < timedCondition.length()
							&& Character.isDigit(timedCondition.charAt(xInt))) {
						xInt++;
					}
					xIntEnd = xInt;

					timedAutomata.setX(Integer.parseInt(timedCondition
							.substring(xIntStart, xIntEnd)));

				}

				if (timedCondition.contains("y")) {
					int yIndex = timedCondition.indexOf("y");
					String symbol = timedCondition.substring(yIndex + 1,
							yIndex + 2);
					if (symbol.equals("<")) {
						timedAutomata.setYSymbol(0);
					} else if (symbol.equals("=")) {
						timedAutomata.setYSymbol(1);
					} else if (symbol.equals(">")) {
						timedAutomata.setYSymbol(2);
					} else if (symbol.equals(":")) {
						timedAutomata.setYSymbol(3);
					}

					int yIntStart = yIndex + 2, yIntEnd;
					while (!Character.isDigit(timedCondition.charAt(yIntStart))) {
						yIntStart++;
					}

					int yInt = yIntStart;
					while (yInt < timedCondition.length()
							&& Character.isDigit(timedCondition.charAt(yInt))) {
						yInt++;
					}
					yIntEnd = yInt;
					timedAutomata.setY(Integer.parseInt(timedCondition
							.substring(yIntStart, yIntEnd)));
				}

				timedAutomataSet.addTimedAutomata(timedAutomata);
			}
			timedAutomataReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static Boolean GetMessageFromMessageLog(String TimedMessagesFileName) {// ��ӵ�����
		// TODO Auto-generated method stub

		try {
			BufferedReader messageReader = new BufferedReader(new FileReader(
					TimedMessagesFileName));

			String s = new String();

			while ((s = messageReader.readLine()) != null) {
				if (s.contains(":") && s.contains("[") && s.contains("]")
						&& s.contains("-") && s.contains("(")
						&& s.contains(")")) {
					String messageTimedCondition = s.substring(0,
							s.indexOf("[") - 1);

					// String messageText = s.substring(s.indexOf("["));

					String type = s.substring(s.indexOf("[") + 1,
							s.indexOf("]"));

					String messageFunction = s.substring(s.indexOf("]") + 1,
							s.indexOf("("));
					while (messageFunction.startsWith(" ")) {
						messageFunction = messageFunction.substring(1);
					}

					String parameter = s.substring(s.indexOf("(") + 1,
							s.indexOf(")"));

					String returnValue = "";
					if (s.contains("):"))
						returnValue = s.substring(s.lastIndexOf("):") + 2);
					Message m = new Message(messageTimedCondition, type,
							messageFunction, parameter, returnValue);
					m.setMessageOrigin(s);
					messageLog.add(m);
				}
			}

			messageReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static Boolean GetMessageFromMessageSequence(
			List<String> TimedMessages) {
		// TODO Auto-generated method stub
		if (TimedMessages.size() == 0) {
			return true;
		}

		messageLog.clear();

		for (int j = 0; j < TimedMessages.size(); j++) {
			String s = TimedMessages.get(j);

			if (s.contains(":") && s.contains("[") && s.contains("]")
					&& s.contains("-") && s.contains("(") && s.contains(")")) {
				String messageTimedCondition = s.substring(0,
						s.indexOf("[") - 1);

				// String messageText = s.substring(s.indexOf("["));

				String type = s.substring(s.indexOf("[") + 1, s.indexOf("]"));

				String messageFunction = s.substring(s.indexOf("]") + 1,
						s.indexOf("("));
				while (messageFunction.startsWith(" ")) {
					messageFunction = messageFunction.substring(1);
				}

				String parameter = s.substring(s.indexOf("(") + 1,
						s.indexOf(")"));

				String returnValue = "";
				if (s.contains("):"))
					returnValue = s.substring(s.lastIndexOf("):") + 2);
				Message m = new Message(messageTimedCondition, type,
						messageFunction, parameter, returnValue);
				m.setMessageOrigin(s);
				messageLog.add(m);
			}
		}

		return true;
	}

}
