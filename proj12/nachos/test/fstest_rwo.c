#include "stdlib.h"
#include "syscall.h"
#include "stdio.h"

int main(){

	int i, fd0, fd1, fd_invalid, count_all, count_half;
	int toRet0, toRet1;

	int close0_status;
	int close1_status;
	int close_invalid;

	char* write_buffer[] = {"This is the first thing that I have written into a file.\n",
			        "Max is breathing really hard next to me and it is kind of awkward.\n"};

	char read_buffer0[100]; 
	char read_buffer1[100]; 

	fd0 = creat("Alex.txt");
	fd1 = creat("Max.txt");
	fd_invalid = 5;

	count_all = strlen(write_buffer[0]);
	count_half = strlen(write_buffer[1])/2;

	toRet0 = write(fd0, write_buffer[0], count_all);
	toRet1 = write(fd1, write_buffer[1], count_half);

	
	printf("\nFile Descriptor right after creat for Alex.txt: %d\n", fd0);
	printf("File Descriptor right after creat for Alex.txt: %d\n\n", fd1);
	

	printf("Number of bytes written to Alex.txt is : %d\n", toRet0);
	printf("Number of bytes written to Max.txt is : %d\n\n", toRet1); 

	close(fd0);
	close(fd1);

	open("Alex.txt");
	open("Max.txt");


	toRet0 = read(fd0, read_buffer0, 100);
	toRet1 = read(fd1, read_buffer1, 100);


        printf("Number of bytes read from Alex.txt is : %d\n", toRet0);
	printf("Number of bytes read from Max.txt is : %d\n\n", toRet1);

	read_buffer0[toRet0]= '\0';
	read_buffer1[toRet1] = '\0';

	printf("String read into buff0: %s\n" ,  read_buffer0);
	printf("String read into buff1: %s\n\n", read_buffer1);	

	close0_status = close(fd0);
	close1_status = close(fd1);
	close_invalid = close(fd_invalid);

	printf("Alex.txt was closed with status %d\n", close0_status);
	printf("Max.txt was closed with status %d\n\n", close1_status);

	
	for(i = 4; i < 17; i++){
		close_invalid = close(i);
		printf("Attempted to close invalid file and has status %d\n\n", close_invalid);
	}

	//Attempt to write into a closed file
	toRet0 = write(fd0, write_buffer[0], count_all);
	toRet1 = write(fd1, write_buffer[1], count_half);

	printf("Attempted to write to closed file, Alex.txt, num bytes written : %d\n", toRet0);
	printf("Attempted to write to closed file, Max.txt, num bytes written  : %d\n\n", toRet1); 


	//Open some files that are closed
	fd0 = open("Alex.txt");
	fd1 = open("Max.txt");

	printf("Opened file Alex.txt and returned file descriptor: %d\n", fd0);
	printf("Opened file Max.txt and returned file descriptor: %d\n\n", fd1);

	write_buffer[0] = "This is what I'm writing to Alex.txt";

	write(fd0, write_buffer[0], strlen(write_buffer[0]));
	

	halt();
}

	
	
