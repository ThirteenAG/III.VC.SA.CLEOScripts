SCRIPT_START
{
    LVAR_INT scplayer, car 
    LVAR_INT pLib, pfGetCurrentFOV, pfSetFOVMultiplier, pfRemoveFOVMultiplier
    LVAR_FLOAT carSpeed, carMaxSpeed, carFOV, carFOVMult
    LVAR_INT tempVar1, tempVar2, tempVar3
    LVAR_INT crc32

    CONST_INT player 0
    GET_PLAYER_CHAR player scplayer

IF LOAD_DYNAMIC_LIBRARY "GTASA.WidescreenFix.asi" pLib
    GET_DYNAMIC_LIBRARY_PROCEDURE "GetCurrentFOV" pLib (pfGetCurrentFOV)
    GET_DYNAMIC_LIBRARY_PROCEDURE "SetFOVMultiplier" pLib (pfSetFOVMultiplier)
    GET_DYNAMIC_LIBRARY_PROCEDURE "RemoveFOVMultiplier" pLib (pfRemoveFOVMultiplier)
ENDIF

IF NOT pfGetCurrentFOV > 0
OR NOT pfSetFOVMultiplier > 0
OR NOT pfRemoveFOVMultiplier > 0
    WHILE timera < 5000
        WAIT 0
        PRINT_STRING_NOW "~r~Unable to load GTASA.WidescreenFix.asi. Make sure you have latest Widescreen Fix installed." 1000
    ENDWHILE
    TERMINATE_THIS_CUSTOM_SCRIPT
ENDIF

GET_VAR_POINTER carFOV (tempVar1)
crc32 = 0x5cfa7496 //CarSpeedDependantFOV
    
    WHILE scplayer >= 0
    WAIT 0

        IF IS_PLAYER_PLAYING player
        AND IS_CHAR_IN_ANY_CAR scplayer
            STORE_CAR_CHAR_IS_IN_NO_SAVE scplayer car
            GET_CAR_SPEED car carSpeed
            if carSpeed > 0.0f
                GET_VEHICLE_POINTER car (tempVar2)
                tempVar2 += 0x384 // m_pHandlingData
                READ_MEMORY tempVar2 4 FALSE (tempVar2)
                tempVar2 += 0x88  // m_fMaxSpeed
                READ_MEMORY tempVar2 4 FALSE (carMaxSpeed)
                carMaxSpeed *= 60.0f
                carFOVMult = 1.0f / carMaxSpeed
                carFOVMult *= carSpeed
                carFOVMult /= 3.0f
                carFOVMult += 1.0f
                IF carFOVMult > 1.2f
                   carFOVMult = 1.2f
                ENDIF
            ELSE
                GOSUB setDefault
            ENDIF     
        ELSE
            GOSUB setDefault
        ENDIF

        //CALL_FUNCTION pfGetCurrentFOV 1 1 (tempVar1)
        //PRINT_FORMATTED_NOW "%f %f %f %f" 4 carFOV carSpeed carMaxSpeed carFOVMult
        CALL_FUNCTION pfSetFOVMultiplier 2 2 (carFOVMult crc32)
        
    ENDWHILE
TERMINATE_THIS_CUSTOM_SCRIPT

///////////////////////////////////////////////////////////////////////////////////////////////////

setDefault:
IF carFOVMult > 1.0f
    carFOVMult -= 0.1f
ENDIF
IF carFOVMult < 1.0f
    carFOVMult = 1.0f
ENDIF
RETURN

}
SCRIPT_END
