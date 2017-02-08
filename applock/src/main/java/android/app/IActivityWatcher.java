/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/zhuolin/work/project/lzl/PrivacyLock/PrivacyLock/src/main/java/android/app/IActivityWatcher.aidl
 */
package android.app;
/**
 * Callback interface to watch the user's traversal through activities.
 */
public interface IActivityWatcher extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements android.app.IActivityWatcher
{
private static final java.lang.String DESCRIPTOR = "android.app.IActivityWatcher";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an android.app.IActivityWatcher interface,
 * generating a proxy if needed.
 */
public static android.app.IActivityWatcher asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof android.app.IActivityWatcher))) {
return ((android.app.IActivityWatcher)iin);
}
return new android.app.IActivityWatcher.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_activityResuming:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.activityResuming(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_closingSystemDialogs:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.closingSystemDialogs(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements android.app.IActivityWatcher
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void activityResuming(int activityId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(activityId);
mRemote.transact(Stub.TRANSACTION_activityResuming, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void closingSystemDialogs(java.lang.String reason) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(reason);
mRemote.transact(Stub.TRANSACTION_closingSystemDialogs, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_activityResuming = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_closingSystemDialogs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void activityResuming(int activityId) throws android.os.RemoteException;
public void closingSystemDialogs(java.lang.String reason) throws android.os.RemoteException;
}
