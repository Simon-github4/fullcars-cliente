package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import Utils.MovementType;
import Utils.ServerException;
import data.service.ClienteRestStockMovement;
import model.client.entities.StockMovement;

public class StockMovementController {
	
	private final ClienteRestStockMovement serviceStockMovement = new ClienteRestStockMovement();
	
	public StockMovement getStockMovement(Long id){
		return serviceStockMovement.getStockMovement(id);
	}
	
	public List<StockMovement> getStockMovements(){
		return serviceStockMovement.getStockMovements();
	}

	public List<StockMovement> getStockMovements(LocalDate[] dates, boolean entries, boolean exits){
		List<StockMovement> movements;
		if(dates != null)
			movements = serviceStockMovement.getStockMovements(dates[0], dates[1]);
		else
			movements = getStockMovements();
		
		Set<MovementType> tipos = EnumSet.noneOf(MovementType.class);
	    if (entries) {
	        tipos.add(MovementType.ENTRADA_COMPRA);
	        tipos.add(MovementType.ENTRADA_AJUSTE);
	    }
	    if (exits) {
	        tipos.add(MovementType.SALIDA_VENTA);
	        tipos.add(MovementType.SALIDA_AJUSTE);
	    }
	    return movements.stream()
	            .filter(m -> tipos.contains(m.getType()))
	            .collect(Collectors.toList());
	}
	
	public void save(StockMovement c) throws ServerException, IOException, Exception {
		//TODO crearf Referencias
		serviceStockMovement.save(c);
	}

	public void delete(Long id) throws ServerException, IOException {
		serviceStockMovement.delete(id);
	}
}
