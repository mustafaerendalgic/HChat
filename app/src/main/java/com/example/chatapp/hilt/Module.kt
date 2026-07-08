package com.example.chatapp.hilt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import androidx.room.Room
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.data.repo.BluetoothRepo
import com.example.chatapp.bluetooth.data.repo.client.ClientHandlerImp
import com.example.chatapp.bluetooth.data.repo.general.GeneralHandlerImp
import com.example.chatapp.bluetooth.room.BluetoothDao
import com.example.chatapp.bluetooth.room.RoomDatabaseForBluetooth
import com.example.chatapp.bluetooth.data.local.SPDataHandlers
import com.example.chatapp.bluetooth.data.repo.BleConnectionManager
import com.example.chatapp.bluetooth.data.repo.BleDiscoveryManager
import com.example.chatapp.bluetooth.data.repo.BluetoothMessageParser
import com.example.chatapp.bluetooth.data.repo.general.SPHandler
import com.example.chatapp.scan.data.repo.NetworkScanRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule{

    @Provides
    @Singleton
    fun getBluetoothManager(@ApplicationContext context: Context): BluetoothManager{
        return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    @Provides
    @Singleton
    fun getBluetoothAdapter(bm: BluetoothManager): BluetoothAdapter{
        return bm.adapter
    }

    @Provides
    @Singleton
    fun getDAO(database: RoomDatabaseForBluetooth): BluetoothDao{
        return database.bluetoothMessageDao()
    }

    @Provides
    @Singleton
    fun getRoomDatabase(@ApplicationContext context: Context): RoomDatabaseForBluetooth{
        return Room.databaseBuilder(context, RoomDatabaseForBluetooth::class.java, ObjectConstants.DATABASE_NAME).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun getRepo(dao: BluetoothDao, adapter: BluetoothAdapter, spHandler: SPHandler, bleMessageParser: BluetoothMessageParser, bleConnectionManager: BleConnectionManager, bleDiscoveryManager: BleDiscoveryManager): BluetoothRepo{
        return BluetoothRepo(dao, adapter, spHandler, bleMessageParser, bleConnectionManager, bleDiscoveryManager)
    }

    @Provides
    @Singleton
    fun getClientHandler(repo: BluetoothRepo): ClientHandlerImp{
        return ClientHandlerImp(repo)
    }

    @Provides
    @Singleton
    fun getGeneralHandler(repo: BluetoothRepo): GeneralHandlerImp{
        return GeneralHandlerImp(repo)
    }

    @Provides
    @Singleton
    fun getNetworkRepo(wm: WifiManager): NetworkScanRepo{
        return NetworkScanRepo(wm)
    }

    @Provides
    @Singleton
    fun getWifiManager(@ApplicationContext context: Context): WifiManager{
        return context.getSystemService(WIFI_SERVICE) as WifiManager
    }

    @Provides
    @Singleton
    fun getSPHandler(@ApplicationContext context: Context): SPHandler{
        return SPDataHandlers(context)
    }

    @Provides
    @Singleton
    fun getBleMessageParser(): BluetoothMessageParser{
        return BluetoothMessageParser()
    }

}