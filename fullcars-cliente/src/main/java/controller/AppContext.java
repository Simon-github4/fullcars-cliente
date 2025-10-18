package controller;

import data.service.LoginService.User;
import interfaces.IBrandProvider;

public class AppContext {
	
    public static final CarPartController carPartController = new CarPartController();
    public static final CategoryController categoryController = new CategoryController();
    public static final IBrandProvider brandProvider = new BrandController();
    public static final ProviderController providerController = new ProviderController();
    public static final SaleController saleController = new SaleController();
    //public static final ProviderController providerController = new ProviderController();
    public static User user;

    public static void setUser(User u) {
		user = u;
	}
    public static User getUser() {
    	return user;
    }
    //descentralize controller to not pass by parameter all.
    //for example from CarPartDialog, to not pass unnecesary params through PurchaseForm
}

