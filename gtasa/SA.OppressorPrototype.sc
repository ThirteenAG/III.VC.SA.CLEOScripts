SCRIPT_START
{
    LVAR_INT scplayer
    LVAR_INT car
    LVAR_INT carStruct
    LVAR_FLOAT player_x player_y player_z
    LVAR_FLOAT exhaust_x exhaust_y exhaust_z
    LVAR_INT fx
    LVAR_INT blipColor
    LVAR_FLOAT carSpeed
    LVAR_INT isOppressor
    LVAR_INT chargeCount
    LVAR_INT tempVar, tempVar2, speedCheck

    CONST_INT player 0
    CONST_INT CVehicleFlyingControl 0x6D85F0
    CONST_INT playerBlipColor 0x588687

    //replacing speed value(?) for when bike is in the air, removing annoying sound
    speedCheck = 150
    GET_VAR_POINTER speedCheck tempVar
    WRITE_MEMORY 0x4FC937 4 tempVar 1
    WRITE_MEMORY 0x4FC41A 4 tempVar 1

    //restoring player's blip color
    WRITE_MEMORY playerBlipColor 1 0xFF 1 

    GET_PLAYER_CHAR 0 scplayer
    
    WHILE scplayer >= 0
    WAIT 0

        IF IS_PLAYER_PLAYING player
        AND IS_CHAR_IN_MODEL scplayer PCJ600
            STORE_CAR_CHAR_IS_IN_NO_SAVE scplayer car
            GET_VEHICLE_POINTER car carStruct
            tempVar = carStruct + 0x42
            READ_MEMORY tempVar 1 1 isOppressor
            SET_CAR_PROOFS car 0 1 0 1 1
                IF NOT isOppressor = 0x38
                    IF CLEO_CALL is_car_at_specified_coordinates 0 car 435 2527 16 //airport
                        SET_CAR_PROOFS car 0 1 0 1 1
                    ENDIF
                ELSE
                    //PRINT_FORMATTED_NOW "%d" 1 chargeCount

                    IF IS_BUTTON_PRESSED PAD1 LEFTSTICKY
                    OR IS_CAR_IN_AIR_PROPER car
                        speedCheck = 15000 // annoying engine sfx
                        IF NOT IS_BUTTON_PRESSED PAD1 RIGHTSHOCK
                            CALL_METHOD CVehicleFlyingControl carStruct 5 0 -9999.9902 -9999.9902 -9999.9902 -9999.9902 5
                        ENDIF
                    ELSE
                        speedCheck = 150 // annoying engine sfx
                    ENDIF

                    IF  IS_BUTTON_PRESSED PAD1 RIGHTSHOCK
                    AND chargeCount >= 50
                        timerb = -5000
                        CLEO_CALL getExhaustPipeOffset 0 car exhaust_x exhaust_y exhaust_z
                        CREATE_FX_SYSTEM_ON_CAR_WITH_DIRECTION "explosion_fuel_car" car exhaust_x exhaust_y exhaust_z 0.0 0.0 0.0 1 fx
                    ENDIF

                    IF NOT IS_CAR_IN_AIR_PROPER car
                    AND chargeCount < 50
                    AND NOT IS_BUTTON_PRESSED PAD1 RIGHTSHOCK
                    AND timerb > 0
                        timera = -5000
                    ENDIF

                    IF timera < 0
                        chargeCount += 1

                        tempVar2 = chargeCount * 5
                        blipColor = 255
                        blipColor -= tempVar2
                        WRITE_MEMORY playerBlipColor 1 blipColor 1 

                            IF chargeCount >= 50
                                timera = 0
                            ENDIF
                    ENDIF

                    IF timerb < 0
                        GET_CAR_SPEED car carSpeed
                        carSpeed += 10.0
                        SET_CAR_FORWARD_SPEED car carSpeed
                        chargeCount -= 2

                        PLAY_FX_SYSTEM fx
                        REPORT_MISSION_AUDIO_EVENT_AT_CAR car SOUND_EXPLOSION

                        tempVar2 = chargeCount * 5
                        blipColor = 255
                        blipColor -= tempVar2
                        WRITE_MEMORY playerBlipColor 1 blipColor 1 

                           IF chargeCount <= 0
                               timerb = 0
                               KILL_FX_SYSTEM fx
                           ENDIF
                    ENDIF
                ENDIF
        ELSE
            //restoring speed check and blip color
            speedCheck = 150
            WRITE_MEMORY playerBlipColor 1 0xFF 1 
        ENDIF

    ENDWHILE

}
SCRIPT_END

{
is_car_at_specified_coordinates:
LVAR_INT car
LVAR_INT x y z
LVAR_FLOAT player_x player_y player_z
LVAR_INT int_x int_y int_z
GET_CAR_COORDINATES car player_x player_y player_z
CSET int_x player_x
CSET int_y player_y
CSET int_z player_z
    IF  int_x = x
    AND int_y = y
    AND int_z = z
        IS_PC_VERSION       // gives true
    ELSE
        IS_AUSTRALIAN_GAME  // gives false
    ENDIF
CLEO_RETURN 0
}

{
getExhaustPipeOffset:
LVAR_INT var
LVAR_FLOAT x y z
GET_VEHICLE_POINTER var var
var += 0x22 
READ_MEMORY var 2 1 var //model index
var *= 4
var += 0xA9B0C8
READ_MEMORY var 4 1 var //CModel
var += 0x5C                                        
READ_MEMORY var 4 1 var //vehicle struct
var += 0x48
READ_MEMORY var 4 1 x
var += 4
READ_MEMORY var 4 1 y
var += 4
READ_MEMORY var 4 1 z
CLEO_RETURN 0 x y z
}