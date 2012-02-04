import java.io.BufferedReader;
import java.io.FileReader;

public class Modifier {

	public static void main(String args[]) {
		new Modifier();
	}

	public Modifier() {
		String str = readFile("idou.csv");
		String[][] data = parseCSV(str);
		double diff_ido=0;
		double diff_keido=0;
		double ido_kyori=0;
		double keido_kyori = 0;
		double kyori_span = 0;
		double kyori=0;
		double diff_time=0;
		//計算可能用に格納
		double[][] data_k = new double[data.length][3];
		for(int i=0; i<data.length;i++){
			data_k[i][0]=Double.valueOf(data[i][0]).doubleValue();
			data_k[i][1]=Double.valueOf(data[i][1]).doubleValue();
			data_k[i][2]=Double.valueOf(data[i][2]).doubleValue();
			if(i>0) {
				diff_ido = data_k[i][1]-data_k[i-1][1];
				diff_keido = data_k[i][2]-data_k[i-1][2];
				ido_kyori = diff_ido*31*3600;
				keido_kyori = diff_keido*25.153129*3600;
				kyori_span = Math.sqrt(ido_kyori*ido_kyori+keido_kyori*keido_kyori);
				kyori=kyori+kyori_span;
			}
		}
		diff_time = data_k[data_k.length][0]-data_k[0][0];
		diff_time = diff_time/1000/60;
		kyori=kyori/1000;
		System.out.println("合計移動距離："+kyori+"km,合計移動時間："+diff_time+"分");
	}
	public String[][] parseCSV(String str){

		String[] str1 = str.split("\n");
		String[][] strs2 = new String[str1.length][3];
		for(int i=0;i<str1.length;i++) {
			strs2[i] = str1[i].split(",");
		}
		return strs2;
	}
	public String readFile(String fName) {
		try{
			FileReader f = new FileReader(fName);
			BufferedReader b = new BufferedReader(f);
			String str="";
			String s;
			while((s = b.readLine())!=null){
				str=str+s+"\n";
			}
			return str;
		}catch(Exception e){
			System.out.println("ファイル読み込み失敗");
		}
		return null;
	}
}
