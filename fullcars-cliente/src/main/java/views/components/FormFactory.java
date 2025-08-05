package views.components;

import controller.BrandController;
import controller.CarPartController;
import controller.CategoryController;
import controller.CustomerController;
import controller.ProviderController;
import controller.PurchaseController;
import controller.SaleController;
import controller.StockMovementController;
import views.BrandForm;
import views.CarPartForm;
import views.CategoryForm;
import views.CustomerForm;
import views.ProviderForm;
import views.PurchaseForm;
import views.PurchaseHistory;
import views.SaleForm;
import views.SalesHistory;
import views.StockMovementForm;

public class FormFactory {

	private static final CarPartController CAR_PART_CONTROLLER = new CarPartController();
	private static final BrandController BRAND_CONTROLLER = new BrandController();
	private static final CategoryController CATEGORY_CONTROLLER = new CategoryController();	    
	private static final CustomerController CUSTOMER_CONTROLLER = new CustomerController();	    
	private static final ProviderController PROVIDER_CONTROLLER = new ProviderController();	    
	private static final StockMovementController STOCKMOVEMENT_CONTROLLER = new StockMovementController();
	private static final SaleController SALE_CONTROLLER = new SaleController();	    
	private static final PurchaseController PURCHASE_CONTROLLER = new PurchaseController();	    
	    
    public static CarPartForm createFormCarPart() {
        return new CarPartForm(CAR_PART_CONTROLLER, BRAND_CONTROLLER, CATEGORY_CONTROLLER, PROVIDER_CONTROLLER);
    }
	
    public static CategoryForm createCategoriesForm() {
        return new CategoryForm(CATEGORY_CONTROLLER);
    }

	public static BrandForm createBrandsForm() {
		return new BrandForm(BRAND_CONTROLLER);
	}

	public static CustomerForm createCustomerForm() {
		return new CustomerForm(CUSTOMER_CONTROLLER);
	}

	public static ProviderForm createProviderForm() {
		return new ProviderForm(PROVIDER_CONTROLLER);
	}

	public static StockMovementForm createStockMovementForm() {
		return new StockMovementForm(STOCKMOVEMENT_CONTROLLER, CAR_PART_CONTROLLER);
	}

	public static SalesHistory createSalesHistory() {
		return new SalesHistory(SALE_CONTROLLER, CUSTOMER_CONTROLLER);
	}

	public static SaleForm createSalesForm() {
		return new SaleForm(SALE_CONTROLLER, CAR_PART_CONTROLLER, CUSTOMER_CONTROLLER);
	}

	public static PurchaseForm createPurchaseForm() {
		return new PurchaseForm(PURCHASE_CONTROLLER, CAR_PART_CONTROLLER, PROVIDER_CONTROLLER);
	}

	public static PurchaseHistory createPurchaseHistory() {
		return new PurchaseHistory(PURCHASE_CONTROLLER, PROVIDER_CONTROLLER);
	}

}
