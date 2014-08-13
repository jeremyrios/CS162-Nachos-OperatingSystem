#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

#define INVALID_PID -1
#define ERROR -1

int main(int argc, const char* argv[]){
	
	char* invalid_file;
	char* invalid_file2;
	char* valid_file;
	char* matmult_file;
	
	int* status;
	int exec_return_val;
	int join_return_val;
	int creat_return_val;
	int open_return_val;
	int read_return_val;
	int write_return_val;
	int close_return_val;
	int unlink_return_val;

	
	invalid_file = "Invalid.coff";
	invalid_file2 = "Invalid2.coff";
	valid_file = "task1_test.coff";
	matmult_file = "matmult.coff";
	
	creat_return_val = creat(1);
	assert(creat_return_val == ERROR);

	creat_return_val = creat(1234);
	assert(creat_return_val == ERROR);

	read_return_val = read(16, "buggggssssss", 100023);
	assert(read_return_val == ERROR);

	write_return_val = write(16, "more buggggsss", 13423);
	assert(write_return_val == ERROR);

	close_return_val = close(16);
	assert(close_return_val == ERROR);

	unlink_return_val = unlink(123412);
	assert(unlink_return_val == ERROR);

	open_return_val = open(status);
	assert(open_return_val == ERROR);

	exec_return_val = exec(invalid_file, 57, argv);
	assert(exec_return_val == ERROR);

	exec_return_val = exec(invalid_file2, argc, argv);
	assert(exec_return_val == ERROR);

	exec_return_val = exec("wadup", -8, argv);
	assert(exec_return_val == ERROR);

	exec_return_val = exec(1, argc, "yo");
	assert(exec_return_val == ERROR);

	join_return_val = join("thug nasty", status);
	assert(join_return_val == ERROR);

	join_return_val = join(INVALID_PID, status);
	assert(join_return_val == ERROR);

	return 0;
}
	
