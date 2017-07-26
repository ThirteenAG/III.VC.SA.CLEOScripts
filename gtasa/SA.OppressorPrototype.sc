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
    LVAR_INT timerc

    CONST_INT player 0
    CONST_INT CVehicleFlyingControl 0x6D85F0
    CONST_INT playerBlipColor 0x588687
    CONST_INT addr_of_6D8B54 0x6D8B54

    //replacing speed value(?) for when bike is in the air, removing annoying sound
    speedCheck = 150
    GET_VAR_POINTER speedCheck tempVar
    WRITE_MEMORY 0x4FC937 4 tempVar 1
    WRITE_MEMORY 0x4FC41A 4 tempVar 1

    //restoring player's blip color
    WRITE_MEMORY playerBlipColor 1 0xFF 1 

    //making turns in air easier
    WRITE_MEMORY addr_of_6D8B54 4 0x858CEC 1 //0.039

    GET_PLAYER_CHAR 0 scplayer
    
    WHILE scplayer >= 0
    WAIT 0
    timerc += 1

        IF IS_PLAYER_PLAYING player
        AND IS_CHAR_IN_MODEL scplayer PCJ600
            STORE_CAR_CHAR_IS_IN_NO_SAVE scplayer car
            GET_VEHICLE_POINTER car carStruct
            tempVar = carStruct + 0x42
            READ_MEMORY tempVar 1 1 isOppressor
                //SET_CAR_PROOFS car 0 1 0 1 1 //for tests only
                IF NOT isOppressor = 0x38
                    IF CLEO_CALL is_car_at_specified_coordinates 0 car 435 2527 16 //airport
                        SET_CAR_PROOFS car 0 1 0 1 1
                    ENDIF
                ELSE
                    //PRINT_FORMATTED_NOW "%d" 1 chargeCount

                    IF IS_BUTTON_PRESSED PAD1 CIRCLE //shoot
                         SET_CURRENT_CHAR_WEAPON scplayer 0
                         CLEO_CALL fireOneInstantHitRoundFromHeadLight 0 car
                    ENDIF

                    IF IS_BUTTON_PRESSED PAD1 LEFTSHOCK //horn
                    //AND IS_CAR_IN_AIR_PROPER car
                    AND timerc > 0
                         CLEO_CALL fireRocketFromHeadLight 0
                         timerc = -50
                    ENDIF

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
                        CLEO_CALL getDummyOffsetById 0 car 6 exhaust_x exhaust_y exhaust_z
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
fireOneInstantHitRoundFromHeadLight:
LVAR_INT car
LVAR_FLOAT car_x car_y car_z
LVAR_FLOAT car_x2 car_y2 car_z2
LVAR_FLOAT off_x off_y off_z
CLEO_CALL getDummyOffsetById 0 car 0 off_x off_y off_z // headlight id = 0
GET_OFFSET_FROM_CAR_IN_WORLD_COORDS car off_x off_y off_z car_x car_y car_z
off_y += 30.0f
GET_OFFSET_FROM_CAR_IN_WORLD_COORDS car off_x off_y off_z car_x2 car_y2 car_z2
FIRE_SINGLE_BULLET car_x car_y car_z car_x2 car_y2 car_z2 100
LVAR_INT scplayer
GET_PLAYER_CHAR 0 scplayer
REPORT_MISSION_AUDIO_EVENT_AT_CHAR scplayer 1157
CLEO_RETURN 0
}

{
fireRocketFromHeadLight:
LVAR_INT scplayer, car, carStruct, pedStruct, ptr, ptr2, sfx
LVAR_FLOAT car_x car_y car_z
LVAR_FLOAT off_x off_y off_z
GET_PLAYER_CHAR player scplayer
GET_PED_POINTER scplayer pedStruct
STORE_CAR_CHAR_IS_IN_NO_SAVE scplayer car
GET_VEHICLE_POINTER car carStruct
CLEO_CALL getDummyOffsetById 0 car 0 off_x off_y off_z // headlight id = 0
off_y += 0.5f
off_z += 0.5f
GET_OFFSET_FROM_CAR_IN_WORLD_COORDS car off_x off_y off_z car_x car_y car_z
GET_LABEL_POINTER offsets2 ptr
WRITE_MEMORY ptr 4 car_x 1
ptr += 4
WRITE_MEMORY ptr 4 car_y 1
ptr += 4
WRITE_MEMORY ptr 4 car_z 1
GET_LABEL_POINTER offsets2 ptr
GET_LABEL_POINTER CWeapon ptr2
CALL_METHOD 0x73B430 ptr2 2 0 35 35 //cweapon ctor
CALL_METHOD 0x741360 ptr2 5 0 50.0 0 0 ptr carStruct
CLEO_RETURN 0
}
{
offsets2:
DUMP
    00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00
ENDDUMP
CWeapon:
DUMP
    00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00
ENDDUMP
}

{
getDummyOffsetById:
LVAR_INT car, id
LVAR_FLOAT x y z
id *= 0xC
GET_VEHICLE_POINTER car car
car += 0x22 
READ_MEMORY car 2 1 car //model index
car *= 4
car += 0xA9B0C8
READ_MEMORY car 4 1 car //CModel
car += 0x5C                                        
READ_MEMORY car 4 1 car //vehicle struct
car += id
READ_MEMORY car 4 1 x
car += 4
READ_MEMORY car 4 1 y
car += 4
READ_MEMORY car 4 1 z
CLEO_RETURN 0 x y z
}