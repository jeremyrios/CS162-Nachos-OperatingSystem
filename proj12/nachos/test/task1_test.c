#include "stdlib.h"
#include "syscall.h"
#include "stdio.h"

#define BUFFER_LENGTH 100
#define ERROR -1

void check_fd(int fd){
	assert(fd != 0 && fd != 1 && fd > 1 && fd <16);
}

void check_equality(int arg1, int arg2){
	assert(arg1 == arg2);
}

int main(){
	
	int i, fd0, fd1, fd_invalid, count_all, count_half;
	int toRet0, toRet1;

	int close0_status;
	int close1_status;
	int close_invalid;

	char* write_buffer0 = "This is the first thing that I have written into a file.\n";
	char* write_buffer1 = "Max is breathing really hard next to me and it is kind of awkward.\n";
	char* write_buffer_comp = "Max is breathing really hard next";

	char read_buffer0[BUFFER_LENGTH]; 
	char read_buffer1[BUFFER_LENGTH]; 

	int toCompare;
	int sum;
	
	fd0 = creat("Alex.txt");
	fd1 = creat("Max.txt");
	fd_invalid = 5;

	count_all = strlen(write_buffer0);
	count_half = strlen(write_buffer1)/2;

	toRet0 = write(fd0, write_buffer0, count_all);
	toRet1 = write(fd1, write_buffer1, count_half);
	
	check_fd(fd0);
	check_fd(fd1);

	check_equality(toRet0, count_all);
	check_equality(toRet1, count_half);

	close(fd0);
	close(fd1);

	open("Alex.txt");
	open("Max.txt");

	toRet0 = read(fd0, read_buffer0, count_all);
	toRet1 = read(fd1, read_buffer1, count_half);

	check_equality(toRet0, count_all);
	check_equality(toRet1, count_half);

	read_buffer0[toRet0]= '\0';
	read_buffer1[toRet1] = '\0';

	toCompare = (int)(strcmp(read_buffer0,write_buffer0)); 
	assert(toCompare == 0);

	toCompare = (int)(strcmp(read_buffer1,write_buffer_comp)); 
	assert(toCompare == 0);

	close0_status = close(fd0);
	close1_status = close(fd1);
	close_invalid = close(fd_invalid);

	check_equality(close0_status, 0);
	check_equality(close1_status, 0);
	check_equality(close_invalid, ERROR);

	
	for(i = 4; i < 17; i++){
		close_invalid = close(i);
		check_equality(close_invalid, ERROR);
	}

	//Attempt to write into a closed file
	toRet0 = write(fd0, write_buffer0, count_all);
	toRet1 = write(fd1, write_buffer1, count_half);

	check_equality(toRet0, ERROR);
	check_equality(toRet1, ERROR); 


	//Open some files that are closed
	fd0 = open("Alex.txt");
	fd1 = open("Max.txt");

	check_fd(fd0);
	check_fd(fd1);

	write_buffer0 = "This is what I'm writing to Alex.txt";

	write(fd0, write_buffer0, strlen(write_buffer0));

	sum = 0;

	for(i = 0; i < 1000000; i ++){
		sum += i;
	}
		
	printf("%d\n", sum);
	printf("ALL TASK %d TESTS PASSED!!!!!!!!!!!!\n\n",1);
	
	halt();
}

	
	
