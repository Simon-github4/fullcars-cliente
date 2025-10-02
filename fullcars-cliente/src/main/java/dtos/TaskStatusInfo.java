package dtos;

public class TaskStatusInfo {
    
	private final TaskStatus status;
    private final String error;
    
    public TaskStatusInfo(TaskStatus status, String error) {
        this.status = status;
        this.error = error;
    }
    public TaskStatusInfo() {
		this.status = null;
		this.error = null;
    }
    public TaskStatus getStatus() { return status; }
    public String getError() { return error; }

    public enum TaskStatus {
        PENDIENTE, CARGANDO, TERMINADO, FALLADO, NO_ENCONTRADO
    }

}