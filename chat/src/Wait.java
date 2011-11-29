import java.net.*;
import java.util.*;

public class Wait extends Thread {
	//リスト
	public static ArrayList<Chat> chat_list = new ArrayList<Chat>();
	//接続受付サーバーソケット
	public ServerSocket server;
	public Socket sock;
	
	public String host = "localhost";
	//ポート番号
	public int port = 3333;
	
	public Wait() {
		//初期化処理へ
		init();
	}
	//初期化処理
	public void init() {
		try {
			//ServerSocketを生成
				server = new ServerSocket(port);
			} catch (Exception e) {
				System.out.println("この" + port + "接続無理っす");
			}
		System.out.println("Server : Listening to the port #" + port +"......");
	}
	//接続されたら新しいリストを作る
	public void run() {
		try {
			while (true) {
				sock = server.accept();
				Chat chat = new Chat(sock, this);
				chat_list.add(chat);
				chat.start();
			}
		} catch (Exception e) {
			System.out.println("閉じちゃった");
			System.exit(1);
		}
	}
	//排他制御で文字列を全てのリストに送る
	synchronized void broadcast(String message) {
		for (int i = 0; i < chat_list.size(); i++) {
			Chat chat = (Chat)chat_list.get(i);
			chat.cast(message);
		}
	}

}
