// **********************************************************************
//
// Copyright (c) 2003-2010 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.4.1

package Glacier2;

// <auto-generated>
//
// Generated from file `PermissionsVerifier.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>


public interface _PermissionsVerifierDel extends Ice._ObjectDel
{
    boolean checkPermissions(String userId, String password, Ice.StringHolder reason, java.util.Map<String, String> __ctx)
        throws IceInternal.LocalExceptionWrapper;
}