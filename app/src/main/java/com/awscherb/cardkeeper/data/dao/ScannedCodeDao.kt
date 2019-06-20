package com.awscherb.cardkeeper.data.dao

import androidx.room.*
import com.awscherb.cardkeeper.data.model.ScannedCode
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ScannedCodeDao {

    @Query("SELECT * FROM scannedCode WHERE id = :id LIMIT 1")
    suspend fun getScannedCode(id: Int): ScannedCode

    @Query("SELECT * FROM scannedCode")
    suspend fun listScannedCodes(): List<ScannedCode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: ScannedCode)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCode(code: ScannedCode)

    @Delete
    suspend fun deleteCode(code: ScannedCode)
}