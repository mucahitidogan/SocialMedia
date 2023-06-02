package com.bilgeadam.config.security;

import com.bilgeadam.utility.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

//OncePerRequestFilter --> Her bir istek için bir kere çalışarak bir filtreleme işlemi sağlıyor (Spring Framework sınıfıdır)
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private JwtUserDetails jwtUserDetails;

    /**
     * HttpServlet(senkron) --> WebFlax(asenkron), OpenFeign, HttpClient, RestTemplate
     * HttpServlet, Java Servlet API tarafından sağlanan bir sınıftır ve HTTP protokolü üzerinden gelen istekleri işlemek için kullanılır.
     * Servlet, dinamik web uygulamaları geliştirmek için kullanılır ve doğrudan "web sunucusunda" çalışır.
     * HTTP istekleri(GET, POST, DELETE, UPDATE, OPTİONS vb..)
     *
     * HttpServletRequest --> Bir HTTP isteğinin sorgusunu ilgili sunucuya gönderir. İstemci tarafından sunucuya gönderilen isteği içerir.
     *                        Örn; istek başlıkları, parametreler, request yolu, HTTP yöntemi
     * HttpServletResponse --> Sunucudan gelen cevabı yakalıyor.
     *
     * FilterChain --> İsteği işlemek ve yanıt üretmek için bir dizi filtreyi içeren bir yapıdır.
     *                 Bir HTTP isteği ServletRequest' e geldiğinde burada bir filtreleme yapılır ve sunucya bu şekilde gönderilir.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeaderToken = request.getHeader("Authorization");
        System.out.println("Header'dan gelen token bilgisi --> " + authHeaderToken);

        if (authHeaderToken != null && authHeaderToken.startsWith("Bearer ")){
            String token = authHeaderToken.substring(7);
            System.out.println("Bearer' ı silinmiş token --> " + token);
            Optional<Long> authId = jwtTokenProvider.getIdFromToken(token);
            //kullanıcının göndermiş olduğu token bilgisinin role ve kimlik bilgilerinin doğrulanması
            //UserDetails userDetails = jwtUserDetails.loadUserByUsername();
        }

        filterChain.doFilter(request, response);
    }
}