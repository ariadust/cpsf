
/*
 *           echo_server.c 
 * echo server program for Ubiquitous System Architecture. 
 * - 16 May 2007: Created by Hiroshi Sakakibara <skk@ht.sfc.keio.ac.jp>
 */

#include <sys/types.h>  // socket(), listen(), accept(), read(), write()
#include <sys/socket.h> // socket(), listen(), accept(), read(), write()
#include <sys/uio.h>    // read(), write()

#include <netinet/in.h> // struct sockaddr_in 

#include <string.h> // memset()
#include <stdio.h>  // printf() 
#include <unistd.h> // read(), write()

int
main()
{
	int sockfd, acceptfd; 
	int len; 
	char buf[1024];
	struct sockaddr_in server, client; 

	/* create socket */
	sockfd = socket(PF_INET,SOCK_STREAM,0);
	

	/* initialize socket */
	memset(&server, 0, sizeof(server)); 
	server.sin_family = PF_INET; 
	server.sin_port   = htons(7000); 
	server.sin_addr.s_addr = htonl(INADDR_ANY); 
	bind(sockfd,(struct sockaddr*)&server,sizeof(struct sockaddr_in));

	/* wait here until client tries to connect */
	listen(sockfd,5);

	/* establish connection between a client host */
	memset(&client, 0, sizeof(client)); 
	len = sizeof(client); 
	acceptfd = accept(sockfd,(struct sockaddr*)&client,&len);
	
	/* open data */
	int fd = open("test.txt",O_RDONLY);
	
	/* read data */
	char buf[1024];
	read(fd,buf,sizeof(buf));
	printf("accept data: %s\n", buf); 

	/* write data */
	write(1,buf,strlen(buf));

	/* close procedure */
	close(fd);
	close(acceptfd); 
	close(sockfd); 

	exit(0); 

}
