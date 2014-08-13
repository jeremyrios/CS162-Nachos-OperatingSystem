#include "stdio.h"


int main(){

	char* filenames[] = {"     ", "File1.txt","File2.txt", "File3.txt","File4.txt", "File5.txt",
			     "File6.txt", "File7.txt","File8.txt", "File9.txt", "File10.txt", "File11.txt",
			     "File12.txt", "File13.txt", "File14.txt", "File15.txt", "    "};
	int fd0, fd1, fd2, fd3, fd4, fd5, fd6, fd7, fd8, fd9, fd10, fd11, fd12, fd13, fd14, fd15;

	fd0 = creat(filenames[0]);
	fd1 = creat(filenames[1]);
	fd2 = creat(filenames[2]);
	fd3 = creat(filenames[3]);
	fd4 = creat(filenames[4]);
	fd5 = creat(filenames[5]);
	fd6 = creat(filenames[6]);
	fd7 = creat(filenames[7]);
	fd8 = creat(filenames[8]);
	fd9 = creat(filenames[9]);
	fd10 = creat(filenames[10]);
	fd11 = creat(filenames[11]);
	fd12 = creat(filenames[12]);
	fd13 = creat(filenames[13]);
	fd14 = creat(filenames[14]);
	fd15 = creat(filenames[15]);
	

	printf("Filename: %s\n File Descriptor: %d\n", filenames[0], fd0);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[1], fd1);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[2], fd2);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[3], fd3);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[4], fd4);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[5], fd5);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[6], fd6);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[7], fd7);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[8], fd8);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[9], fd9);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[10], fd10);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[11], fd11);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[12], fd12);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[13], fd13);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[14], fd14);
	printf("Filename: %s\n File Descriptor: %d\n", filenames[15], fd15);
	
	printf("Attempting to create an already created file0 fd: %d\n", creat(filenames[0]));
        printf("Attempting to create an already created file3 fd: %d\n", creat(filenames[3]));
        printf("Attempting to create an already created file4 fd: %d\n", creat(filenames[4]));
        printf("Attempting to create an already created file7 fd: %d\n", creat(filenames[7]));
        printf("Attempting to create an already created file6 fd: %d\n", creat(filenames[6]));
	halt();
}
