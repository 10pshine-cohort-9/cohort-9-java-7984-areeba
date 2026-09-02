package com.tenpearls.contactmanagement.security;



import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.web.filter.OncePerRequestFilter;



import java.io.IOException;



public class JwtAuthenticationFilter extends OncePerRequestFilter {



    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;



    public JwtAuthenticationFilter(

            JwtService jwtService,

            CustomUserDetailsService userDetailsService,

            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint

    ) {

        this.jwtService = jwtService;

        this.userDetailsService = userDetailsService;

        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;

    }



    @Override

    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {



        String authHeader = request.getHeader("Authorization");



        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);

            return;

        }



        try {

            String token = authHeader.substring(7).trim();

            String email = jwtService.extractEmail(token);



            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);



                if (jwtService.isTokenValid(token, userDetails.getUsername())) {

                    UsernamePasswordAuthenticationToken authentication =

                            new UsernamePasswordAuthenticationToken(

                                    userDetails,

                                    null,

                                    userDetails.getAuthorities()

                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                }

            }



            filterChain.doFilter(request, response);

        } catch (JwtException | UsernameNotFoundException exception) {

            SecurityContextHolder.clearContext();

            jwtAuthenticationEntryPoint.commence(

                    request,

                    response,

                    new BadCredentialsException("Invalid token", exception)

            );

        }

    }

}


