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
	
	invalid_file = "Invalid.coff";
	invalid_file2 = "Invalid2.coff";
	valid_file = "task1_test.coff";
	matmult_file = "matmult.coff";
	

	exec_return_val = exec(invalid_file, argc, argv);
	assert(exec_return_val == ERROR);

	exec_return_val = exec(invalid_file2, argc, argv);
	assert(exec_return_val == ERROR);

	exec_return_val = exec(valid_file, argc, argv);
	assert(exec_return_val >= 0);

	//exec_return_val = exec(matmult_file, argc, argv);
	//assert(exec_return_val >= 0);

	join_return_val = join(exec_return_val, status);
	assert(join_return_val == 1);

	join_return_val = join(INVALID_PID, status);
	assert(join_return_val == ERROR);

	return 0;
}
	
