package tasks;

public enum TaskStatus {
	SENDED,
	FAILED,
	RESCHEDULED,
	ERROR,
	SCHEDULE,
	TERMINATED_OK, //its not part of statistics
	COMPLETE, //its now part of statistics
	FAIL_EXCEPTION_RESCHEDULE
}
