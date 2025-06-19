package Utils;

import controller.BrandController;
import controller.CarPartController;
import controller.CategoryController;
import interfaces.IBrandProvider;
import views.FormCarPart;

public class FormFactory {

	private static final CarPartController CAR_PART_CONTROLLER = new CarPartController();
	private static final BrandController BRAND_CONTROLLER = new BrandController();
	private static final CategoryController CATEGORY_CONTROLLER = new CategoryController();	    
	    
    public static FormCarPart createFormCarPart() {
        return new FormCarPart(CAR_PART_CONTROLLER, BRAND_CONTROLLER, CATEGORY_CONTROLLER);
    }
	

}
