package munji;

import java.awt.BorderLayout; 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Vector; 
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel; 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException; 
import javax.swing.JTable; 
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;

public class MunjiInfo extends JDialog { 
	/*
	 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15073861
	 */
	private static final long serialVersionUID = 1L; 
	private static final String servicekey =""; //����Ű
	private static final String numOfRows ="1000"; // �� ������ �ִ� �����
	private static final String pageNo ="1"; // ��������ȣ
	private static final String sidoDefault ="����"; // �õ� �̸�(����, ����..)
	private static final String ver ="1.0"; // ������ �� ���
	private static final String[] modelSet = //ã�� ������
			{"sidoName","stationName","dataTime","so2Value","so2Grade","coValue","coGrade","o3Value","o3Grade",
			"no2Value","no2Grade","pm10Value","pm10Grade","pm25Value","pm25Grade"};
	private static final String[] columnSet = //������ ǥ�ø�
			{"��/��","������","�����ð�","��Ȳ�갡�� ��","���","�ϻ�ȭź�� ��","���","���� ��","���",
			 "�̻�ȭ���� ��","���","�̼�����PM10 ��","���","�̼�����PM2.5 ��","���"}; 
	private static final String[] sidolist = {//�õ�
			"����", "����", "�λ�", "�뱸", "��õ", "����", "����", "���", "���", "����", "���", "�泲", "����", "����", 
			"���", "�泲", "����", "����"};
	
	
	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private DefaultTableModel dtm;
	private Vector<String> columnNames;
	private int totalcount; 
	private final JPanel panel = new JPanel();
	private final JLabel lblNewLabel = new JLabel("\uC9C0\uC5ED \uC774\uB984 :"); 
	private final JButton btnNewButton = new JButton("\uAC80\uC0C9");
	private final JComboBox<String> comboBox = new JComboBox<String>(sidolist);
	private JScrollPane scrollPane = new JScrollPane();
	
	public static void main(String[] args) {
		try {
			new MunjiInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	
	public MunjiInfo()  throws IOException, ParseException { 
		Vector<Vector<String>> MunjiData = getMunji(sidoDefault);
		columnNames= new Vector<String>(Arrays.asList(columnSet)); 
		dtm =  new DefaultTableModel(MunjiData,columnNames);
		table = new JTable(dtm);
		setTitle(sidoDefault+" ��ȸ��� : �� "+totalcount+"��");
		 
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String search = comboBox.getSelectedItem().toString();
				Vector<Vector<String>> newMunjiData = getMunji(search);
				dtm = new DefaultTableModel(newMunjiData,columnNames);
				table.setModel(dtm);
				dtm.fireTableDataChanged();
				setTitle(search+" ��ȸ��� : �� "+totalcount+"��");
			}
		});
		
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
		setSize(1500,640);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0, 0));
		contentPanel.add(scrollPane, BorderLayout.CENTER); 
		contentPanel.add(panel, BorderLayout.NORTH); 
		scrollPane.setViewportView(table); 
		panel.add(lblNewLabel); 
		panel.add(comboBox); 
		panel.add(btnNewButton);
	}
	 
	private Vector<Vector<String>> getMunji(String sidoName) { 
		try { // api url ����
	        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty"); 
	        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "="+servicekey);
	        urlBuilder.append("&" + URLEncoder.encode("returnType","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
	        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8"));
	        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8"));
	        urlBuilder.append("&" + URLEncoder.encode("sidoName","UTF-8") + "=" + URLEncoder.encode(sidoName, "UTF-8")); 
	        urlBuilder.append("&" + URLEncoder.encode("ver","UTF-8") + "=" + URLEncoder.encode(ver, "UTF-8"));
	        URL url = new URL(urlBuilder.toString());
			 
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-type", "application/json"); 
	        BufferedReader rd;
	        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
	            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
	        } else {
	            return null;
	        }
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) {
	            sb.append(line);
	        }
	        rd.close();
	        conn.disconnect();
	  
	        JSONParser jsonParser = new JSONParser(); //json �Ľ�
	        JSONObject jsonObj = (JSONObject) jsonParser.parse(sb.toString()); 
	        JSONObject body = (JSONObject)((JSONObject)jsonObj.get("response")).get("body");
	        totalcount = Integer.parseInt(body.get("totalCount").toString());
	        JSONArray jsonArr = (JSONArray) body.get("items");
	        Vector<Vector<String>> totalDatas = new Vector<>(); 
	        
		    if (jsonArr.size() > 0){
		        for(int i=0; i<jsonArr.size(); i++){
		        	JSONObject item = (JSONObject)jsonArr.get(i);
		        	Vector<String> itemData = new Vector<>();  
		        	for(int j=0;j<modelSet.length;j++) {
		        		if(item.get(modelSet[j])==null) { //�����Ͱ� ������ '-' ����
		        			itemData.add("-");
		        		}else {
		        			itemData.add(item.get(modelSet[j]).toString());
		        		} 
		        	} 
		        	totalDatas.add(itemData);
		        }
		    } 
			return totalDatas; 
		}catch(Exception err){
			err.printStackTrace();
			return null;
		}
	} 
	 
}
