package com.iwayvietnam.zmsso.cas;

import com.iwayvietnam.zmsso.BaseSsoHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CasSloHandler extends BaseSsoHandler {
    @Override
    public String getPath() {
        return CasSsoConstants.SLO_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    }
}
