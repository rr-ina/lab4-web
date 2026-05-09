package com.example.ssl.binance;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "binance")
public class BinanceProperties {
    private String websocketUrl;
    private List<String> currencies;

    public String getWebsocketUrl() { 
        return websocketUrl; 
    }
    public void setWebsocketUrl(String websocketUrl) { 
        this.websocketUrl = websocketUrl; 
    }

    public List<String> getCurrencies() { 
        return currencies; 
    }

    public void setCurrencies(List<String> currencies) { 
        this.currencies = currencies; 
    }
}