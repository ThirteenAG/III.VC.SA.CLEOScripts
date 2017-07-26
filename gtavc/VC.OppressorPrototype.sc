SCRIPT_START
{
    LVAR_INT scplayer
    LVAR_INT car
    LVAR_INT carStruct
    LVAR_FLOAT player_x player_y player_z
    LVAR_INT blipColor
    LVAR_FLOAT speedCheck, carSpeed
    LVAR_INT sfx
    LVAR_INT isOppressor
    LVAR_INT chargeCount
    LVAR_INT tempVar
    LVAR_FLOAT tempVar2
    LVAR_INT timerc

    CONST_INT player 0
    CONST_INT dword_60D174 0x60D174
    CONST_INT playerBlipColor 0x4C2FFA
    CONST_INT CVehicleFlyingControl 0x5B54C0
    CONST_INT engineSfxSound 0x5F18BB
    CONST_INT cDMAudioPlayOneShot 0x5F9DA0
    CONST_INT bAudioClass 0xA10B8A
    CONST_INT exhaustParticleId 0x60D4A3
    CONST_INT addr_of_flt_69C724 0x5B5AA0
    CONST_INT addr_of_5C769C 0x5C769C

    //replacing speed value when exhaust particle disappear
    speedCheck = 130.0f
    GET_VAR_POINTER speedCheck tempVar
    WRITE_MEMORY dword_60D174 4 tempVar 1
    //restoring player's blip color
    WRITE_MEMORY playerBlipColor 1 0xFF 1
    //making turns in air easier
    WRITE_MEMORY addr_of_flt_69C724 4 0x6B272C 1 //0.039
    //missiles hunter id
    WRITE_MEMORY addr_of_5C769C 4 155 1 //hunter

    GET_PLAYER_CHAR 0 scplayer
    
    WHILE scplayer >= 0
    WAIT 0
    timerc += 1

        IF IS_PLAYER_PLAYING player
        AND IS_PLAYER_IN_MODEL player PCJ600
            STORE_CAR_PLAYER_IS_IN_NO_SAVE player car
            GET_VEHICLE_POINTER car carStruct
            tempVar = carStruct + 0x53
            READ_MEMORY tempVar 1 1 isOppressor

                IF NOT isOppressor = 0x1C
                    IF CLEO_CALL is_car_at_specified_coordinates 0 car -596 676 10 //near bikers
                        SET_CAR_PROOFS car 0 1 0 1 1
                    ENDIF
                ELSE
                    //PRINT_FORMATTED_NOW "%x" 1 carStruct
	                
                    IF IS_BUTTON_PRESSED PAD1 CIRCLE //shoot
                         SET_CURRENT_CHAR_WEAPON scplayer 0
                         CLEO_CALL fireOneInstantHitRoundFromHeadLight 0
                    ENDIF

                    IF IS_BUTTON_PRESSED PAD1 LEFTSHOCK //horn
                    //AND IS_CAR_IN_AIR_PROPER car
                    AND timerc > 0
                         WRITE_MEMORY addr_of_5C769C 4 191 1 //PCJ600
                         CLEO_CALL fireRocketFromHeadLight 0 car
                         timerc = -50
                    ENDIF

                    IF IS_BUTTON_PRESSED PAD1 RIGHTSTICKY
                    OR IS_CAR_IN_AIR_PROPER car
                        WRITE_MEMORY engineSfxSound 4 265 1 // annoying engine sfx
                        IF NOT IS_BUTTON_PRESSED PAD1 RIGHTSHOCK
                            CALL_METHOD CVehicleFlyingControl carStruct 1 0 1
                        ENDIF
                    ELSE
                        WRITE_MEMORY engineSfxSound 4 264 1 // annoying engine sfx
                    ENDIF

                    IF IS_BUTTON_PRESSED PAD1 RIGHTSHOCK
                    AND chargeCount >= 50
                        timerb = -5000
                    ENDIF

                    IF NOT IS_CAR_IN_AIR_PROPER car
                    AND chargeCount < 50
                    AND NOT IS_BUTTON_PRESSED PAD1 RIGHTSHOCK
                    AND timerb > 0
                        timera = -5000
                    ENDIF

                    IF timera < 0
                        chargeCount += 1
                        blipColor = chargeCount * 5
                        INT_SUB 255 blipColor blipColor //blipColor = 255 - blipColor
                        WRITE_MEMORY playerBlipColor 4 blipColor 1
                            IF chargeCount >= 50
                                timera = 0
                            ENDIF
                    ENDIF

                    IF timerb < 0
                        GET_CAR_SPEED car carSpeed
                        carSpeed += 10.0
                        SET_CAR_FORWARD_SPEED car carSpeed
                        chargeCount -= 2

                        GET_PED_POINTER scplayer sfx
                        sfx += 0x64
                        READ_MEMORY sfx 4 1 sfx
                        CALL_METHOD_RETURN cDMAudioPlayOneShot bAudioClass 3 0 0.0 58 sfx sfx

                        speedCheck = 330.0
                        WRITE_MEMORY exhaustParticleId 1 42 1 

                        blipColor = chargeCount * 5
                        INT_SUB 255 blipColor blipColor //blipColor = 255 - blipColor
                        WRITE_MEMORY playerBlipColor 1 blipColor 1 

                           IF chargeCount <= 0
                               timerb = 0
                               speedCheck = 130.0
                               WRITE_MEMORY exhaustParticleId 1 67 1 
                           ENDIF
                    ENDIF
                ENDIF
        ELSE
            //restoring speed check and blip color
            speedCheck = 130.0f
            WRITE_MEMORY exhaustParticleId 1 67 1
            WRITE_MEMORY playerBlipColor 1 0xFF 1
            WRITE_MEMORY addr_of_5C769C 4 155 1 //hunter
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
LVAR_INT car, ptr, ptr2, sfx
LVAR_FLOAT car_x car_y car_z
LVAR_FLOAT off_x off_y off_z
STORE_CAR_PLAYER_IS_IN_NO_SAVE player car
CLEO_CALL getDummyOffsetById 0 car 0 off_x off_y off_z // headlight id = 0
GET_OFFSET_FROM_CAR_IN_WORLD_COORDS car off_x off_y off_z car_x car_y car_z
GET_LABEL_POINTER offsets ptr
WRITE_MEMORY ptr 4 car_x 1
ptr += 4
WRITE_MEMORY ptr 4 car_y 1
ptr += 4
WRITE_MEMORY ptr 4 car_z 1
off_y += 30.0f
GET_OFFSET_FROM_CAR_IN_WORLD_COORDS car off_x off_y off_z car_x car_y car_z
ptr += 4
WRITE_MEMORY ptr 4 car_x 1
ptr += 4
WRITE_MEMORY ptr 4 car_y 1
ptr += 4
WRITE_MEMORY ptr 4 car_z 1
GET_LABEL_POINTER offsets ptr
ptr2 = ptr + 0xC
CALL_FUNCTION 0x5C9BB0 3 3 1000 ptr2 ptr
GET_VEHICLE_POINTER car sfx
sfx += 0x64
READ_MEMORY sfx 4 1 sfx
CALL_METHOD_RETURN cDMAudioPlayOneShot bAudioClass 3 0 0.0 55 sfx sfx
CLEO_RETURN 0
}
{
offsets:
DUMP
    00 00 00 00 00 00 00 00 00 00 00 00
    00 00 00 00 00 00 00 00 00 00 00 00
ENDDUMP
}

{
fireRocketFromHeadLight:
LVAR_INT car, carStruct, ptr, ptr2, sfx
LVAR_FLOAT car_x car_y car_z
LVAR_FLOAT off_x off_y off_z
STORE_CAR_PLAYER_IS_IN_NO_SAVE player car
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
CALL_METHOD 0x5D4E20 ptr2 2 2 20 30 //cweapon ctor
CALL_METHOD 0x5CCF90 ptr2 3 3 50.0 ptr carStruct
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
LVAR_INT carStruct, mID, mBase
LVAR_FLOAT car_x car_y car_z
LVAR_FLOAT off_x off_y off_z
GET_VEHICLE_POINTER car carStruct
carStruct += 0x5C
READ_MEMORY carStruct 4 1 mID
mID *= 4
mID += 0x92D4C8
READ_MEMORY mID 4 1 mBase
id *= 0xC
id += 0x50
mBase += id
READ_MEMORY mBase 4 1 off_x
mBase += 4
READ_MEMORY mBase 4 1 off_y
mBase += 4
READ_MEMORY mBase 4 1 off_z
CLEO_RETURN 0 off_x off_y off_z
}