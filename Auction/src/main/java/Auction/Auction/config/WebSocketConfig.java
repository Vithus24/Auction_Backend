package Auction.Auction.config;

import Auction.Auction.security.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.logging.Logger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger LOGGER = Logger.getLogger(WebSocketConfig.class.getName());

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for dev (broadcasts to /topic)
        // Reason: Simple broker is in-memory, fast for local testing. For production/AWS, replace with Redis for distributed pub/sub:
        // config.enableStompBrokerRelay("/topic").setRelayHost("redis-endpoint").setRelayPort(6379).setSystemLogin("user").setSystemPasscode("pass");
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");  // Prefix for message handlers (e.g., /app/bid)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register endpoint for clients to connect (ws://localhost:8080/ws-auction)
        // Reason: /ws-auction is secure; clients connect here. withSockJS() provides fallback for old browsers. setAllowedOriginPatterns allows flexible origins (http/https, ports).
        // In production, restrict to specific origins (e.g., "https://yourdomain.com") to prevent cross-site connections.
        registry.addEndpoint("/ws-auction")
                .setAllowedOriginPatterns("*")  // Allow all for dev; change to List.of("http://localhost:3000", "https://yourdomain.com") in prod
                .withSockJS();  // Fallback; remove if only modern browsers
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register interceptor for inbound messages (e.g., CONNECT)
        // Reason: Validates JWT before allowing connection/subscriptions, ensuring security at the entry point.
        registration.interceptors(new JwtChannelInterceptor(jwtTokenProvider));
    }

    /**
     * Intercepts CONNECT frames and validates JWT from headers.
     */
    private static class JwtChannelInterceptor implements ChannelInterceptor {

        private final JwtTokenProvider jwtTokenProvider;

        public JwtChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
            this.jwtTokenProvider = jwtTokenProvider;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");  // Expect "Bearer <token>"

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    LOGGER.warning("Missing or invalid Authorization header");
                    throw new IllegalArgumentException("Missing Authorization header");  // Reject connection
                }

                String token = authHeader.substring(7).trim();
                if (!jwtTokenProvider.validateToken(token)) {
                    LOGGER.warning("Invalid JWT token");
                    throw new IllegalArgumentException("Invalid JWT token");  // Reject connection
                }

                String email = jwtTokenProvider.getEmailFromToken(token);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        email, null, jwtTokenProvider.getAuthorities(token));  // Uses role from claims
                accessor.setUser(authentication);  // Set principal for handlers
            }
            return message;
        }
    }
}