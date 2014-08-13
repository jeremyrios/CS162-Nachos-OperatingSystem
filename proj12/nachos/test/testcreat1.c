#include "stdlib.h"
#include "syscall.h"
#include "stdio.h"

int main()
{
  int fd;  
  char * fileName;
  
  printf("Simple test of creat()\n");

  fileName = "foo.txt";
  fd = creat(fileName);

  printf("File Descriptor = %d\n", fd);

  fd = creat(fileName);

  halt();
}
