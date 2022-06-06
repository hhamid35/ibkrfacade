package hu.auxin.ibkrfacade;

import com.ib.client.Contract;
import com.ib.client.Types;
import hu.auxin.ibkrfacade.data.ContractRepository;
import hu.auxin.ibkrfacade.data.holder.ContractHolder;
import hu.auxin.ibkrfacade.data.holder.OrderHolder;
import hu.auxin.ibkrfacade.data.holder.PositionHolder;
import hu.auxin.ibkrfacade.data.holder.PriceHolder;
import hu.auxin.ibkrfacade.service.ContractManagerService;
import hu.auxin.ibkrfacade.service.OrderManagerService;
import hu.auxin.ibkrfacade.service.PositionManagerService;
import hu.auxin.ibkrfacade.twssample.ContractSamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web endpoints for accessing TWS functionality
 */
@RestController
@DependsOn("TWS")
public class WebHandler {

    private ContractRepository contractRepository;
    private ContractManagerService contractManagerService;
    private OrderManagerService orderManagerService;
    private PositionManagerService positionManagerService;

    /**
     * This variable decides if "high frequency" data stream is needed from IBKR.
     * It's value can be set from application.properties file.
     */
    @Value("${ibkr.tick-by-tick-stream}")
    private boolean tickByTickStream;

    @Autowired
    WebHandler(ContractRepository contractRepository, ContractManagerService contractManagerService,
               OrderManagerService orderManagerService, PositionManagerService positionManagerService) {
        this.contractRepository = contractRepository;
        this.contractManagerService = contractManagerService;
        this.orderManagerService = orderManagerService;
        this.positionManagerService = positionManagerService;
    }

    @Operation(summary = "Search for an instrument by it's ticker, or part of it's name.",
        parameters = {
            @Parameter(description = "Ticker, or name of the traded instrument", examples = {
                    @ExampleObject(name = "Ticker", value = "AAPL"),
                    @ExampleObject(name = "Company name", value = "Apple")
            })
        }
    )
    @GetMapping("/search")
    List<ContractHolder> searchContract(@RequestParam String query) {
        return contractManagerService.searchContract(query).stream()
                .map(ContractHolder::new)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Subscribes to an instrument by it's conid. Subscription means the TWS starts streaming the market data for the instrument which will be saved into Redis TimeSeries",
        parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
        }
    )
    @GetMapping("/subscribe")
    void subscribeMarketDataByConid(@RequestParam int conid) {
        Contract contract = ContractSamples.ByConId();
        contract.conid(conid);
        contractManagerService.subscribeMarketData(contract, tickByTickStream);
    }

    @Operation(summary = "Subscribes to an instrument by the Contract entity sent in request body. Subscription means the TWS starts streaming the market data for the instrument which will be saved into Redis TimeSeries",
        parameters = {
            @Parameter(name = "contract", description = "The Contract descriptor object as a JSON.")
        }
    )
    @PostMapping("/subscribe")
    void subscribeMarketDataByContract(@RequestBody Contract contract) {
        contractManagerService.subscribeMarketData(contract, tickByTickStream);
    }

    @Operation(summary = "Returns with the ContractHolder which contains the Contract descriptor itself and the streamRequestId if you are already subscribed to the instrument.",
        parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
        }
    )
    @GetMapping("/contract/{conid}")
    ContractHolder getContractByConid(@PathVariable int conid) {
        return contractRepository.findById(conid).orElse(new ContractHolder(contractManagerService.getContractByConid(conid)));
    }

    @Operation(summary = "Sends an order to the market.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument"),
            @Parameter(name = "action", description = "BUY or SELL"),
            @Parameter(name = "quantity", description = "Quantity"),
            @Parameter(name = "price", description = "Price value")
    })
    @PostMapping("/order")
    void placeOrder(@RequestParam int conid, @RequestParam String action, @RequestParam double quantity, @RequestParam double price) {
        Contract contract = contractRepository.findById(conid)
                .orElse(new ContractHolder(contractManagerService.getContractByConid(conid)))
                .getContract();
        orderManagerService.placeLimitOrder(contract, Types.Action.valueOf(action), quantity, price);
    }

    @Operation(summary = "Returns with the list of orders.")
    @GetMapping("/orders")
    Collection<OrderHolder> getAllOrders() {
        return orderManagerService.getAllOrders();
    }

    @Operation(summary = "Returns with the list of active orders.")
    @GetMapping("/orders/active")
    Collection<OrderHolder> getActiveOrders() {
        return orderManagerService.getActiveOrders();
    }

    @Operation(summary = "Returns with the list of open positions.")
    @GetMapping("/positions")
    Collection<PositionHolder> getAllPositions() {
        return positionManagerService.getAllPositions();
    }

    @Operation(summary = "Returns with the last available bid and ask for the given Contract.", parameters = {
            @Parameter(name = "contract", description = "The Contract descriptor object as a JSON")
    })
    @PostMapping("/price")
    PriceHolder getLastPriceByContract(@RequestBody Contract contract) {
        return contractManagerService.getLastPriceByContract(contract);
    }

    @Operation(summary = "Returns with the last available bid and ask by conid.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
    })
    @GetMapping("/price/{conid}")
    PriceHolder getLastPriceByConid(@PathVariable int conid) {
        return contractManagerService.getLastPriceByConid(conid);
    }
}
