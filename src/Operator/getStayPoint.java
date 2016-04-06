package Operator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
 * author:youg
 * date:20160328
 * ��5goodRecord����ȡÿ���û���ͣ����
 * 0raw:ԭʼ�������ݣ���������γ����Ϣ����ʱ�仮���ļ�
 * 1withPos:���Ӿ�γ����Ϣ����id����λ�����ļ����ļ��ڰ�id��ʱ������
 * 2timeSpan:��¼ÿ��IDÿ��������ֵ�ʱ���λ���Լ��������ֵ�ʱ���λ��
 * 3timeLine����15����Ϊ��λͳ��ÿ��ID��ÿ��ʱ��γ��ֵĴ���
 * 4goodUser:���������á�������һ���������û�ID�б�����ȡ����7��ǰ��19����м�¼��7-19��ÿ3��Сʱ�м�¼���û�����ռ�������û�������55%
 * 5goodRecord:4goodUser�б�����û���������¼����id����λ�ָ��ͬ�ļ���
 * 7stayRecord:��5goodRecord����ȡ�����û�ͣ�����¼
 */
//ԭʼ��
class RawPoint{
	public double lon,lat;
	public long lac,cell;
	public String time;
	public int event;//���������¼�λ����ʱ����
}
//�û�ԭʼ��¼
class RawRecord{
	public String id;
	public String date;
	public List<RawPoint> rawPoints;
	
	public RawRecord(String id, String date){
		this.id = id;
		this.date = date;
		rawPoints = new LinkedList<RawPoint>();
	}
}
//ͣ����
class StayPoint{
	public double lon,lat;
	public String sTime,eTime;
	public int event;//���������¼�λ����ʱ����
	public int state;//1��ͣ���㣻0���ƶ���
}
//�û�ͣ�����¼
class StayRecord{
	public String id;
	public String date;
	public List<StayPoint> stayPoints;
	public StayRecord(String id, String date){
		this.id = id;
		this.date = date;
		stayPoints = new LinkedList<StayPoint>();
	}
}

public class getStayPoint {
	public static String workPath = "F:\\BJmobile\\";
	public static String date = "20130226";
	public static String withPosPathName = workPath+date+"\\1withPos\\";
	public static String timeSpanPathName = workPath+date+"\\2timeSpan\\";
	public static String timeLinePathName = workPath+date+"\\3timeLine\\";
	public static String goodUserPathName = workPath+date+"\\4goodUser\\";
	public static String goodRecordPathName = workPath+date+"\\5goodRecord\\";
	public static String stayRecordPathName = workPath+date+"\\7stayRecord\\";
	public static final int TRD = 1000;//(Threshold of Roaming Distance)�ռ���ֵ,��λ����
	public static final int TRT = 30;//(Threshold of Roaming Time)ʱ����ֵ����λ������
	public static List<RawRecord> rawRecords = new LinkedList<RawRecord>();
	public static List<StayRecord> stayRecords = new LinkedList<StayRecord>();
	//public static long[] stat = new long[12];//ͳ���û�ͣ����ֲ�
	//����RawRecord�û�ԭʼ��¼��Ϣ
	public static void importRawRecord(File goodRecordFileName)throws Exception{
		System.out.println("Now importing RawRecord: "+goodRecordFileName.getAbsolutePath());
		rawRecords.clear();
		BufferedReader br = new BufferedReader(new FileReader(goodRecordFileName));
		String lastId=null,thisId=null;
		String af;
		String[] afs;
		int len=0;
		while((af=br.readLine())!=null){
			afs = af.split(",");
			thisId = afs[0];
			if(!thisId.equals(lastId)){
				RawRecord user = new RawRecord(thisId,afs[1]);
				len+=1;
				rawRecords.add(user);
			}
			RawRecord user = rawRecords.get(len-1);
			RawPoint point = new RawPoint();
			point.lon=Double.valueOf(afs[5]);
			point.lat=Double.valueOf(afs[6]);
			point.lac=Long.valueOf(afs[3]);
			point.cell=Long.valueOf(afs[4]);
			point.time=afs[2];
			point.event=Integer.valueOf(afs[7]);
			user.rawPoints.add(point);
			lastId = thisId;
		}
		br.close();
	}
	//����StayRecord�û�ͣ������Ϣ:����or������չ���÷����ѹ���
	@Deprecated
	public static void calStayRecord_or(){
		System.out.println("Now calculating or...");
		stayRecords.clear();
		for(RawRecord user:rawRecords){
			StayRecord stay = new StayRecord(user.id,user.date);
			List<RawPoint> tempStays = new LinkedList<RawPoint>();
			int start=0;
			while(start<user.rawPoints.size()){
				tempStays.clear();
				int newStart = start;
				tempStays.add(user.rawPoints.get(newStart));
				int newAdd = 1;
				int newEnd = newStart + newAdd;
				while(newAdd>0 && newEnd<user.rawPoints.size()){
					newAdd = 0;
					for(int i=newStart;i<newEnd;i++){
						int j=i+1;
						while(j<user.rawPoints.size() && !tempStays.contains(user.rawPoints.get(j)) && distanceInGlobal(user.rawPoints.get(i),user.rawPoints.get(j))<TRD){
							tempStays.add(user.rawPoints.get(j));
							j+=1;
							newAdd+=1;
						}//end while k
					}//end for j
					newStart = newEnd;
					newEnd = newStart + newAdd;
				}//end while newAdd
				//finish generate tempStays
				//do something with tempStays
				if(tempStays.size()<=1 || timeSpan(tempStays.get(0).time,tempStays.get(tempStays.size()-1).time)<TRT){
					start+=1;
					StayPoint sp = new StayPoint();
					sp.lon = tempStays.get(0).lon;
					sp.lat = tempStays.get(0).lat;
					sp.sTime = tempStays.get(0).time;
					sp.eTime = tempStays.get(0).time;
					sp.event = 0;
					sp.state = 0;
					stay.stayPoints.add(sp);
				}else{
					start+=tempStays.size();
					StayPoint sp = calStayPoint(tempStays);
					stay.stayPoints.add(sp);
				}
			}//end while start
			stayRecords.add(stay);
		}//end for user
	}
	//����StayRecord�û�ͣ������Ϣ:����and������չ
	public static void calStayRecord(){
		System.out.println("Now calculating and...");
		stayRecords.clear();
		for(RawRecord user:rawRecords){
			StayRecord stay = new StayRecord(user.id,user.date);
			List<RawPoint> tempStays = new LinkedList<RawPoint>();
			int start=0;
			while(start<user.rawPoints.size()){
				tempStays.clear();
				int newStart = start;
				tempStays.add(user.rawPoints.get(start));
				int newAdd = 1;
				int newEnd = newStart + newAdd;
				while(newAdd>0 && newEnd<user.rawPoints.size()){
					newAdd = 0;
					int j=newEnd;
					while(j<user.rawPoints.size() && !tempStays.contains(user.rawPoints.get(j))){
						boolean ok=true;
						for(int i=newStart;i<newEnd;i++){
							if(distanceInGlobal(user.rawPoints.get(i),user.rawPoints.get(j))>TRD){
								ok=false;
								break;
							}//end if i,j
						}//end for i
						if(!ok)
							break;
						tempStays.add(user.rawPoints.get(j));
						newAdd+=1;
						j+=1;
					}//end while j
					newStart = newEnd;
					newEnd = newStart + newAdd;
				}//end while newAdd
				//finish generate tempStays
				//do something with tempStays
				if(tempStays.size()<=1 || timeSpan(tempStays.get(0).time,tempStays.get(tempStays.size()-1).time)<TRT){
					start+=1;
					StayPoint sp = new StayPoint();
					sp.lon = tempStays.get(0).lon;
					sp.lat = tempStays.get(0).lat;
					sp.sTime = tempStays.get(0).time;
					sp.eTime = tempStays.get(0).time;
					sp.event = 0;
					sp.state = 0;
					stay.stayPoints.add(sp);
				}else{
					start+=tempStays.size();
					StayPoint sp = calStayPoint(tempStays);
					stay.stayPoints.add(sp);
				}
			}//end while start
			stayRecords.add(stay);
		}//end for user
	}
	//��ͣ����������ÿ��ͣ�����ͣ��ʱ����ΪȨ�ؼ���ͣ����
	public static StayPoint calStayPoint(List<RawPoint> tempStays){
		StayPoint sp = new StayPoint();
		long timeLength,totalTimeLength = 0;
		double mLon = 0.0;
		double mLat = 0.0;
		String prevTime,thisTime,nextTime;
		//�����0����
		thisTime = tempStays.get(0).time;
		nextTime = tempStays.get(1).time;
		timeLength = timeSpan(thisTime,nextTime);
		if(timeLength>1)
			timeLength/=2;
		else
			timeLength=1;
		mLon+=tempStays.get(0).lon*timeLength;
		mLat+=tempStays.get(0).lat*timeLength;
		totalTimeLength+=timeLength;
		//�����1-������2����
		for(int i=1;i<tempStays.size()-1;i++){
			prevTime = tempStays.get(i-1).time;
			thisTime = tempStays.get(i).time;
			nextTime = tempStays.get(i+1).time;
			timeLength = timeSpan(prevTime,thisTime)+timeSpan(thisTime,nextTime);
			if(timeLength>1)
				timeLength/=2;
			else
				timeLength=1;
			mLon+=tempStays.get(i).lon*timeLength;
			mLat+=tempStays.get(i).lat*timeLength;
			totalTimeLength+=timeLength;
		}
		//�������һ����
		prevTime = tempStays.get(tempStays.size()-2).time;
		thisTime = tempStays.get(tempStays.size()-1).time;
		timeLength = timeSpan(prevTime,thisTime);
		if(timeLength>1)
			timeLength/=2;
		else
			timeLength=1;
		mLon+=tempStays.get(tempStays.size()-1).lon*timeLength;
		mLat+=tempStays.get(tempStays.size()-1).lat*timeLength;
		totalTimeLength+=timeLength;
		//�����Ȩƽ��ֵ
		mLon/=totalTimeLength;
		mLat/=totalTimeLength;
		sp.lon = mLon;
		sp.lat = mLat;
		sp.sTime = tempStays.get(0).time;
		sp.eTime = tempStays.get(tempStays.size()-1).time;
		sp.event = 0;
		sp.state = 1;
		return sp;
	}
	//���StayRecord�û�ͣ������Ϣ
	public static void exportStayRecord(String stayRecordFileName)throws Exception{
		stayRecordFileName = stayRecordPathName + stayRecordFileName;
		System.out.println("Now exporting StayRecord: "+stayRecordFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(stayRecordFileName));
		DecimalFormat df = new DecimalFormat("#.000000");
		for(StayRecord user:stayRecords){
			for(StayPoint point:user.stayPoints){
				bw.write(user.id+","+user.date+","+point.sTime+"-"+point.eTime+","+df.format(point.lon)+","+df.format(point.lat)+","+point.state+"\n");
			}
		}
		bw.close();
	}
	/*
	 * ������λ��֮��ľ��룬�����������곤�ȹ�ʽ����(��λ����)
	 * ע�⣬�������ܺ�ʱ��,����,�������Ѿ�γ�ȵ�100�򱶻�ԭ��!
	 */
	public static double distanceInGlobal(double lon1, double lat1, double lon2, double lat2){
		double x1 = lon1;
		double y1 = lat1;
		double x2 = lon2;
		double y2 = lat2;

		double L = (3.1415926*6370/180)*Math.sqrt((Math.abs((x1)-(x2)))*(Math.abs((x1)-(x2)))*(Math.sin((90-(y1))*(3.1415926/180)))*(Math.sin((90-(y1))*(3.1415926/180)))+(Math.abs((y1)-(y2)))*(Math.abs((y1)-(y2))));
		return L * 1000;
	}
	public static double distanceInGlobal(RawPoint a, RawPoint b){
		return distanceInGlobal(a.lon, a.lat, b.lon, b.lat);
	}
	/*
	 * ��������ʱ���֮���ʱ��ʱ���ʽ��HHmmSS�������ؽ����λ����
	 */
	public static int timeSpan(String a, String b){
		int span = Integer.valueOf(b.substring(0,2))*60+Integer.valueOf(b.substring(2,4));
		span = span - (Integer.valueOf(a.substring(0,2))*60+Integer.valueOf(a.substring(2,4)));
		if(span<0)
			span=-span;
		return span;
	}
	//ͳ���û�ͣ���������ֲ�
	/*
	public static void stat(){
		for(StayRecord user:stayRecords){
			int count = 0;
			for(StayPoint point:user.stayPoints){
				if(point.state==1)
					count+=1;
			}
			if(count<=10)
				stat[count]+=1;
			else
				stat[11]+=1;
		}
	}
	*/
	public static void main(String[] args)throws Exception{
		File goodRecordPath = new File(goodRecordPathName);
		File[] goodRecordFiles = goodRecordPath.listFiles();
		int j=0;
		for(File file:goodRecordFiles){
			importRawRecord(file);
			calStayRecord();
			//stat();
			exportStayRecord(file.getName());
			if(++j>=1)
				break;
		}
		System.out.println("finish");
		/*
		for(int i=0;i<12;i++)
			System.out.println(i+":"+stat[i]);
		*/
	}
}