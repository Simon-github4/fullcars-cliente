package views.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controller.BrandController;
import controller.CarPartController;
import controller.CategoryController;
import controller.CustomerController;
import controller.PayController;
import controller.ProviderController;
import controller.PurchaseController;
import controller.SaleController;
import controller.StockMovementController;
import interfaces.Refreshable;
import views.BrandForm;
import views.CarPartForm;
import views.CategoryForm;
import views.CustomerForm;
import views.CustomerSummaryHistory;
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
	private static final PayController PAY_CONTROLLER = new PayController();	    
	    
    public static JPanel createFormCarPart() {
        return new CarPartForm(CAR_PART_CONTROLLER, BRAND_CONTROLLER, CATEGORY_CONTROLLER, PROVIDER_CONTROLLER);
    }
	
    public static JPanel createCategoriesForm() {
        return new CategoryForm(CATEGORY_CONTROLLER);
    }

	public static JPanel createBrandsForm() {
		return new BrandForm(BRAND_CONTROLLER);
	}

	public static JPanel createCustomerForm() {
	    JTabbedPane tabbedPane = new JTabbedPane();
	    
	    JPanel listadoClientesPanel = new CustomerForm(CUSTOMER_CONTROLLER);
	    tabbedPane.addTab("Clientes", listadoClientesPanel);

	    JPanel saldoClientesPanel = new CustomerSummaryHistory(CUSTOMER_CONTROLLER, PAY_CONTROLLER, SALE_CONTROLLER);
	    tabbedPane.addTab("Saldo y Pagos", saldoClientesPanel);

	    tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tabbedPane.getSelectedComponent() instanceof Refreshable )
		           	((Refreshable)tabbedPane.getSelectedComponent()).refresh();
			}
	    });
	    
	    final class RefreshablePanel extends JPanel implements Refreshable {
	        public RefreshablePanel(LayoutManager l) {
				super.setLayout(l);
			}
			@Override
	        public void refresh() {
				if (tabbedPane.getSelectedComponent() instanceof Refreshable )
		           	((Refreshable)tabbedPane.getSelectedComponent()).refresh();
	        }
	    }
	    RefreshablePanel mainPanel = new RefreshablePanel(new BorderLayout());
	    mainPanel.add(tabbedPane, BorderLayout.CENTER);
	    return mainPanel;
	}

	public static JPanel createProviderForm() {
		return new ProviderForm(PROVIDER_CONTROLLER);
	}

	public static JPanel createStockMovementForm() {
		return new StockMovementForm(STOCKMOVEMENT_CONTROLLER, CAR_PART_CONTROLLER);
	}

	public static JPanel createSalesHistory() {
		return new SalesHistory(SALE_CONTROLLER, CUSTOMER_CONTROLLER);
	}

	public static JPanel createSalesForm() {
		return new SaleForm(SALE_CONTROLLER, CAR_PART_CONTROLLER, CUSTOMER_CONTROLLER);
	}

	public static JPanel createPurchaseForm() {
		return new PurchaseForm(PURCHASE_CONTROLLER, CAR_PART_CONTROLLER, PROVIDER_CONTROLLER);
	}

	public static JPanel createPurchaseHistory() {
		return new PurchaseHistory(PURCHASE_CONTROLLER, PROVIDER_CONTROLLER);
	}

}
