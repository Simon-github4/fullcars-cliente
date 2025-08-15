package dtos;

public class ConfirmPurchasePayDTO {

	private boolean isPayed;

	public ConfirmPurchasePayDTO() {}
	public ConfirmPurchasePayDTO(boolean b) {
		this.isPayed = b;
	}
	
	public boolean isPayed() {
		return isPayed;
	}

	public void setPayed(boolean isPayed) {
		this.isPayed = isPayed;
	}
}
