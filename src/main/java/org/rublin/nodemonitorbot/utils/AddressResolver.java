package org.rublin.nodemonitorbot.utils;

import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.exception.TelegramProcessException;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class AddressResolver {
    public static String getIpAddress(String address) {
        try {
            return InetAddress.getByName(address).getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Unknown host: {}", address);
            throw new TelegramProcessException("Unknown host: " + address);
        }
    }
}
