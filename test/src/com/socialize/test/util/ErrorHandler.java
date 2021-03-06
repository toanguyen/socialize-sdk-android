/*
 * Copyright (c) 2012 Socialize Inc. 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.test.util;

import android.content.Context;
import com.socialize.error.SocializeApiError;
import com.socialize.error.SocializeException;
import com.socialize.util.StringUtils;

import java.io.PrintWriter;

public final class ErrorHandler {
	
	public static final String handleApiError(Context context, SocializeException error) {
		if(error instanceof SocializeApiError) {
			SocializeApiError serror = (SocializeApiError) error;
			if(serror.getResultCode() >= 400) {
				if(writeError(context, serror)) {
					return serror.getResultCode() + " Error, file written to device [" +
							context.getFileStreamPath("error.html").getAbsolutePath() +
							"]";
				}
				else {
					return serror.getResultCode() + " Error, no additional info";
				}
			}
			else {
				error.printStackTrace();
			}
		}
		else {
			error.printStackTrace();
		}
		
		return error.getMessage();
	}

	public static final boolean writeError(Context context, SocializeApiError error) {
		PrintWriter writer = null;
		
		try {
			if(!StringUtils.isEmpty(error.getDescription())) {
				writer = new PrintWriter(context.openFileOutput("error.html", Context.MODE_PRIVATE));
				writer.write(error.getDescription());
				writer.flush();
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(writer != null) {
				writer.close();
			}
		}
		
		return false;
		
	}
	
}
