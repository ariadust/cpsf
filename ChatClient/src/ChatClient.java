import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChatClient extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	//ソケット
	Socket sock;
	//チャットログ表示枠
	JTextArea chat_log;
	//チャット参加者表示枠
	JList people;
	DefaultListModel model = new DefaultListModel();
	//入力する場所
	JTextField chat_input;
	//入力用ストリーム
	BufferedReader in;
	//出力用ストリーム
	BufferedWriter out;
	//ホスト
	String host = "localhost";
	//ポート
	int port = 3333;
	
	//スタートアップ
	public static void main(String args[]) {
		(new ChatClient(args)).main();
	}
	//コンストラクタ
	public ChatClient(String args[]) {
		//新しいレイアウトつくる
		getContentPane().setLayout(new BorderLayout(10,0));
		//ログの部分を真ん中に作る
		getContentPane().add(chat_log = new JTextArea(),BorderLayout.CENTER);
		//このテキストエリアは編集できないようにする
		chat_log.setEditable(false);
		getContentPane().add(people = new JList(model),BorderLayout.WEST);
		people.setBackground(new Color(144,255,255));
		//入力部分を下に作る
		getContentPane().add(chat_input = new JTextField(),BorderLayout.SOUTH);
		//全体のサイズ
		setSize(600,400);
		//表示させる
		setVisible(true);
		
		//ボタンが押された時
		addWindowListener(new WindowAdapter() {
			//ウィンドウズを終了する場合
			public void windowClosing(WindowEvent e) {
				System.exit(0);		
		}});
		
		//送信処理(文字を入力するという行動に対しての動作処理)
		chat_input.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				//入力がなされたら動く
				String s = chat_input.getText();
				chat_input.setText("");
				try {
						//改行文字を統一
						if(s.endsWith("\r\n")) {
							out.write(s,0,s.length());
						}else {
							if(s.endsWith("\n")) {
								out.write(s,0,s.length()-1);
							}else {
								out.write(s,0,s.length());
							}
						}
						out.write('\r');
						out.write('\n');
					//文字列をフラッシュする
					out.flush();
				} catch(IOException e) {
					new RuntimeException();
				}
			}
		});
	}
	
	public void main() {
		//文字入力スペースにカーソルがあるよね
		chat_input.requestFocus();
		//接続
		try {
			sock = new Socket(host,port);
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(
					new OutputStreamWriter(sock.getOutputStream()));
		}catch(final IOException e) {
			System.out.println("ふぇぇ"+ host + "　#" + port+"に接続できないよぉ");
			System.exit(1);
		}
		new Thread(this).start();	
	}
	
	public void run() {
		String s;
		String member;
		String s1 = "[";
		String s2;
		String s3 = "さんは魔女になりました";
		String s4;
		boolean flag=false;
		int count = 0;
		int c = 0;
		model.addElement("魔法少女一覧");
		try {
			while(true) {
				
				s = in.readLine();
				if(s == null) {
					break;
				//チャット参加者割り出しシステム
				}else if(s.indexOf(s1)!=-1) {
					s2=s;
					int point = s2.indexOf("]");
					member=s2.substring(1,point);
					System.out.println("member : "+member);
					for(int i=0;i < model.getSize();i++) {
						if(member.equals(model.getElementAt(i))) {
							flag=true;
						}
					}
					if(flag) {
						flag=false;
						System.out.println("残念");
					}else if(member!=model.getElementAt(count)) {
						model.addElement(member);
						count++;
						System.out.println(model.getElementAt(count));
					}
				}else if(s.indexOf(s3)!=-1) {
					s4=s;
					s4=s4.replaceAll("さんは魔女になりました","");
					System.out.println("魔女 : "+ s4);
					for(int i=0;i < model.getSize();i++) {
						if(s4.equals(model.getElementAt(i))) {
							break;
						}
						c++;
					}
					model.removeElementAt(c);
					
					
				}
				//チャットログを改行
				chat_log.append(s + '\n');
			}
		}catch(final IOException e) {
			new RuntimeException();
		}
		System.out.println("Connection closed.\n");
		
	}
}
