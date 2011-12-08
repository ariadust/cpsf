#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <net/if.h>
#include <netinet/in.h>
#include <netinet/if_ether.h>
#include <pcap.h>
#include <arpa/inet.h>
#define SIZE_ETHERNET 14
#define ETHER_ADDR_LEN	6

/*
libpcapのサンプル
ubuntuとかdebianのひとは
libpcap0.8-dev をapt-getとかで入れる
コンパイルするときはライブラリでpcapを指定する
gcc for_cpsf.c -lpcap
詳しくはここにかいてあるので宛先が80番ポートのTCPパケットだけ表示するプログラムを書いてください．
*/

//イーサネットヘッダ
//イーサネットは頭から６バイト分が宛先アドレスで、その次が送信元アドレスになっている。
struct struct_ethernet {
    //受信先のアドレス長さ６で
	u_char  ether_dhost[ETHER_ADDR_LEN];
	//送信先のアドレス長さ６で
	u_char  ether_shost[ETHER_ADDR_LEN];
	//イーサネットのタイプは２バイトなのでshort
	u_short ether_type;
};
//IPアドレスヘッダ
struct ip_header {
	//バージョン（IP通信プロトコルの改訂番号は４）とヘッダ長4bit
	u_char  ip_vhl;            
	//サービスタイプ　8bits     
	u_char  ip_tos;          
	//ヘッダとデータの合計の長さ。16bits       
	u_short ip_len;                 
	//識別子　16bits（分割し再構成するために必要？）
	u_short ip_id;             
	//フラグ　3bits（データグラム分割の有無の判断）
	u_short ip_off;           
//未使用フラグ（必ず０になる）	      
#define IP_RF 0x8000     
//分割禁止フラグ       
#define IP_DF 0x4000
//more fragments flag
#define IP_MF 0x2000            
//フラグメントビットマスク
#define IP_OFFMASK 0x1fff       
	//滞留時間　8bits（ネットワーク内に存在できる時間）
	u_char  ip_ttl;                 
	//プロトコル　8bits（適用されるプロトコルの指定番号）
	u_char  ip_p;                   
	//チェックサム　16bits（ヘッダの中身を16ビット単位で足算し、送り先でも同じ事して比較して一致してるか確かめる）
	u_short ip_sum;               
	//発信先IPアドレスと宛先IPアドレス　それぞれ32bits 
	struct  in_addr ip_src,ip_dst;  
};
#define IP_HL(ip)               (((ip)->ip_vhl) & 0x0f)
#define IP_V(ip)                (((ip)->ip_vhl) >> 4)


//TCPヘッダー
typedef u_int tcp_seq;
struct tcp_header {
	//送信元ポート
	u_short th_sport;
	//送信先ポート               
	u_short th_dport;
	//シーケンス番号             
	tcp_seq th_seq; 
	//確認応答番号
	tcp_seq th_ack;           
	//データオフセットと予約ビット   
	u_char  th_offx2;         
#define TH_OFF(th)      (((th)->th_offx2 & 0xf0) >> 4)
	u_char  th_flags;
#define TH_FIN  0x01
#define TH_SYN  0x02
#define TH_RST  0x04
#define TH_PUSH 0x08
#define TH_ACK  0x10
#define TH_URG  0x20
#define TH_ECE  0x40
#define TH_CWR  0x80
#define TH_FLAGS        (TH_FIN|TH_SYN|TH_RST|TH_ACK|TH_URG|TH_ECE|TH_CWR)
	//ウインドサイズ
	u_short th_win;                 
	//チェックサム
	u_short th_sum;                 
	//緊急ポインタ
	u_short th_urp;             
};

void print_ethaddr(u_char *, const struct pcap_pkthdr *, const u_char *packet);




main(int argc, char *argv[]) {
	//有効なパケットキャプチャディスクリプタ
	pcap_t *pd;
	//取得したいパケットの最大の値
	int snaplen = 64;
	//インタフェースをプロミスキャスモードで動作させるかどうか
    int pflag = 0;
	//読み込みタイムアウト時間
    int timeout = 1000;
	//エラーが発生したときにエラー内容が入る文字列
    char ebuf[PCAP_ERRBUF_SIZE];
	//localnetはIPアドレスが格納されるポインタ、netmaskはネットマスクが格納されるポインタ
    bpf_u_int32 localnet, netmask;
	//handlerをコールバックする
    pcap_handler callback;
    struct bpf_program;

	//macならen0とかubuntuならeth1とか
	//en1は監視対象となるデバイスの名前
    if ((pd = pcap_open_live("en1", snaplen, !pflag, timeout, ebuf)) == NULL) {
		exit(1);
    }	
	//en1は検索したいデバイス名
	if (pcap_lookupnet("eth1", &localnet, &netmask, ebuf) < 0) {
		exit(1);
    }
    callback = print_ethaddr;
    if (pcap_loop(pd, -1, callback, NULL) < 0) {
		exit(1);
    }
	pcap_close(pd);
	exit(0);
}

void print_ethaddr(u_char *args, const struct pcap_pkthdr *header, const u_char *packet){
	//イーサネットのヘッダ
	const struct struct_ethernet *eh; 
	//IPアドレスのヘッダ
	const struct ip_header *ip;
	//ポートのヘッダ             
	const struct tcp_header *tcp; 
	int i;    
	int size_ip;
	int size_tcp;
	int port = 5;
	//イーサネットヘッダ計算
	eh = (struct struct_ethernet *)(packet);
	//IPアドレスヘッダ計算
	ip = (struct ip_header*)(packet + SIZE_ETHERNET);
	//IPアドレス計算
	size_ip = IP_HL(ip)*4;
	//20以下なら戻る
	if(size_ip < 20){
		return;
	}
	//tcpアドレスヘッダ計算
	tcp = (struct tcp_header*)(packet + SIZE_ETHERNET + size_ip);
	//tcpアドレス計算
	size_tcp = TH_OFF(tcp)*4;
	//20以下なら戻る
	if(size_tcp < 20){
		return;
	}
	print("MAC: ");
	
	//送信元MACアドレス
    for (i = 0; i < 6; ++i) {
		printf("%02x", (int)eh->ether_shost[i]);
		if(i < 5){
			printf(":");
		}
	}
    printf(" -> ");
	 //送信先MACアドレス
    for (i = 0; i < 6; ++i) {
		printf("%02x", (int)eh->ether_dhost[i]);
		if(i < 5){
			printf(":");
		}
	}
	printf("\n");
	printf("port : %d -> ",ntohs(tcp->th_sport));
	printf("%d\n",ntohs(tcp->th_dport));
	printf("length: %d\n", ip->ip_len);
	printf("==========================\n");
}
