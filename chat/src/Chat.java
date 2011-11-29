import java.io.*;
import java.net.*;

public class Chat extends Thread {
	
	public Socket sock;//ソケット
	public Wait wait;//チャットサーバー本体
	public BufferedReader in;//入力用ストリーム
	public BufferedWriter out;//出力用ストリーム
	public String handle_name;//ハンドルネーム
	
	//コンストラクタ
	public Chat(Socket sock, Wait wait) {
		this.sock = sock;
		this.wait = wait;
	}
	//クライアントからの入力を受け付ける
	public void run() {
		try {
			String input;
			//ソケットから入出力ストリームを得る
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			
			cast("／人◕‿‿◕人＼: よくきたね");
			cast("／人◕‿‿◕人＼: ボクと契約して君の名前を入力してよ");
			handle_name = in.readLine();
			cast("／人◕‿‿◕人＼: ありがとう。");
			cast("／人◕‿‿◕人＼: 君の名前は, "+handle_name+"だね？じゃあ契約の話をしよう");		
			wait.broadcast(handle_name + "さんが魔法少女になりました。");
			
			//入力待ち
			while (true) {
				//文字入力
				input = in.readLine();
				if(input == null) {
					break;
				}else if(input.equals("あたしって、ほんとバカ")){
					wait.broadcast("["+handle_name+"]:"+input);
					break;
				}
				
				//全クライアントに送る
				wait.broadcast("["+handle_name+"]:"+input);
			}
		} catch (Exception e) {
			new RuntimeException();
		}
		wait.broadcast(handle_name+"さんは魔女になりました");
	}
	//クライアントへ文字列を出力する
	synchronized void cast(String s) {
			try {
				//改行文字を調整
				if(s.endsWith("\r\n")) {
					out.write(s,0,s.length());
				}else {
					if(s.endsWith("\n")) {
						out.write(s,0,s.length()-1);
					}else {
						out.write(s,0,s.length());
					}
					out.write('\r');
					out.write('\n');
				}
				//文字列をフラッシュ！
				out.flush();
			} catch (IOException e) {
				end();
			}
	}
	//終了処理
	public void end() {
		try {
			in.close();
			out.close();
			sock.close();
		}catch(IOException e) {
			new RuntimeException();
		}
	}
}
