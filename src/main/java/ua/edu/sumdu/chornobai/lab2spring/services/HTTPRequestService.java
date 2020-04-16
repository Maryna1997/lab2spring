package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class HTTPRequestService {

    private int connectionTimeout;
    private int readTimeout;

    public HTTPRequestService(@Value("${timeout.connect}") int connectionTimeout,
                              @Value("${timeout.read}") int readTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    final static Logger logger = Logger.getLogger(HTTPRequestService.class);

    @Cacheable("httpResponseResults")
    public String getJSONResult(String url) {
        String result = "";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            connection.connect();

            StringBuilder stringBuilder = new StringBuilder();

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }
                    result = stringBuilder.toString();
                }
            }
        } catch (IOException e) {
            logger.log(Level.FATAL, "Exception: ", e);
            if (e.getClass() == SSLException.class) return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        logger.info("Http response result:" + result);
        return result;
    }
}
