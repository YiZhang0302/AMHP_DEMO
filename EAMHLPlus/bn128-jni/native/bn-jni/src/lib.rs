extern crate bn;
extern crate hex;
extern crate rand;

use bn::arith::U256;
use bn::fields::{Fq12, Fq6};
use bn::{pairing, pairing_batch, AffineG1, AffineG2, Fq, Fq2, Fr, Group, Gt, G1, G2};
use std::error::Error;
use std::fmt;
use std::os::raw::c_int;
use std::os::raw::c_uchar;
use std::os::raw::c_ulong;
use std::slice;

use jni::objects::{JClass, JObject};
use jni::sys::{jboolean, jbyteArray, jint};
use jni::JNIEnv;

#[derive(Debug)]
struct PairingErr {
    code: i32,
    details: String,
}

impl PairingErr {
    fn new(c: i32, msg: &str) -> PairingErr {
        PairingErr {
            code: c,
            details: msg.to_string(),
        }
    }
}

impl fmt::Display for PairingErr {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.details)
    }
}

impl Error for PairingErr {
    fn description(&self) -> &str {
        &self.details
    }
}

impl From<bn::arith::Error> for PairingErr {
    fn from(err: bn::arith::Error) -> Self {
        PairingErr::new(-1, &format!("{:?}", err))
    }
}

impl From<bn::FieldError> for PairingErr {
    fn from(err: bn::FieldError) -> Self {
        PairingErr::new(-2, &format!("{:?}", err))
    }
}

impl From<bn::GroupError> for PairingErr {
    fn from(err: bn::GroupError) -> Self {
        PairingErr::new(-3, &format!("{:?}", err))
    }
}

const FP_SIZE: usize = 32;

#[no_mangle]
pub extern "system" fn Java_org_aion_tetryon_AltBn128Jni_ping(env: JNIEnv, class: JClass) -> jint {
    1337 as jint
}

fn deserialize_g1(pt_byte: &[u8]) -> Result<G1, PairingErr> {
    let x = Fq::from_slice(&pt_byte[0..FP_SIZE])?;
    let y = Fq::from_slice(&pt_byte[FP_SIZE..FP_SIZE * 2])?;
    let pt_af = AffineG1::new(x, y)?;
    Ok(G1::from(pt_af))
}

fn deserialize_g2(pt_byte: &[u8]) -> Result<G2, PairingErr> {
    let x1 = Fq::from_slice(&pt_byte[0..FP_SIZE])?;
    let x2 = Fq::from_slice(&pt_byte[FP_SIZE..FP_SIZE * 2])?;

    let y1 = Fq::from_slice(&pt_byte[FP_SIZE * 2..FP_SIZE * 3])?;
    let y2 = Fq::from_slice(&pt_byte[FP_SIZE * 3..FP_SIZE * 4])?;

    let x = Fq2::new(x1, x2);
    let y = Fq2::new(y1, y2);
    let pt_af = AffineG2::new(x, y)?;
    Ok(G2::from(pt_af))
}

fn deserialize_gt(pt_byte: &[u8]) -> Result<Gt, PairingErr> {
    let index: usize = 12;
    let mut gt_buffer = vec![];
    let mut buffer: [u8; 32] = [0; 32];
    for i in 0..index {
        let item_u256 = Fq::from_slice(&pt_byte[FP_SIZE * (i)..FP_SIZE * (i + 1)])?;
        item_u256.into_u256().to_big_endian(&mut buffer);
        gt_buffer.push(item_u256.into_u256());
    }

    let c0 = bn::fields::Fq::new(gt_buffer[0]).unwrap();
    let c1 = bn::fields::Fq::new(gt_buffer[1]).unwrap();
    let c2 = bn::fields::Fq::new(gt_buffer[2]).unwrap();
    let c3 = bn::fields::Fq::new(gt_buffer[3]).unwrap();
    let c4 = bn::fields::Fq::new(gt_buffer[4]).unwrap();
    let c5 = bn::fields::Fq::new(gt_buffer[5]).unwrap();
    let c6 = bn::fields::Fq::new(gt_buffer[6]).unwrap();
    let c7 = bn::fields::Fq::new(gt_buffer[7]).unwrap();
    let c8 = bn::fields::Fq::new(gt_buffer[8]).unwrap();
    let c9 = bn::fields::Fq::new(gt_buffer[9]).unwrap();
    let c10 = bn::fields::Fq::new(gt_buffer[10]).unwrap();
    let c11 = bn::fields::Fq::new(gt_buffer[11]).unwrap();

    let fq6c0_fq2c0 = bn::fields::Fq2::new(c0, c1);
    let fq6c0_fq2c1 = bn::fields::Fq2::new(c2, c3);
    let fq6c0_fq2c2 = bn::fields::Fq2::new(c4, c5);

    let fq6c1_fq2c0 = bn::fields::Fq2::new(c6, c7);
    let fq6c1_fq2c1 = bn::fields::Fq2::new(c8, c9);
    let fq6c1_fq2c2 = bn::fields::Fq2::new(c10, c11);

    let fq6c0 = bn::fields::Fq6::new(fq6c0_fq2c0, fq6c0_fq2c1, fq6c0_fq2c2);
    let fq6c1 = bn::fields::Fq6::new(fq6c1_fq2c0, fq6c1_fq2c1, fq6c1_fq2c2);

    let fq12 = bn::fields::Fq12::new(fq6c0, fq6c1);
    let new_gt = Gt::new(fq12);
    Ok(new_gt)
}

#[no_mangle]
pub extern "system" fn Java_org_aion_tetryon_AltBn128Jni_g1EcAdd(
    env: JNIEnv,
    class: JClass,
    point1_j: jbyteArray,
    point2_j: jbyteArray,
) -> jbyteArray {
    let p1_byte = env.convert_byte_array(point1_j).unwrap();
    let p2_byte = env.convert_byte_array(point2_j).unwrap();

    let mut p3_byte: [u8; FP_SIZE * 2] = [0; FP_SIZE * 2];
    let ret = alt_bn128_add_internal(&p1_byte, &p2_byte, &mut p3_byte);

    match ret {
        Err(e) => {
            env.throw(format!("{}", e)).unwrap();
            return JObject::null().into_inner();
        }
        Ok(_) => {
            let output = env.byte_array_from_slice(&p3_byte).unwrap();
            return output;
        }
    }
}

#[no_mangle]
pub extern "C" fn alt_bn128_add(
    point1: *const c_uchar,
    len1: c_ulong,
    point2: *const c_uchar,
    len2: c_ulong,
    result: *mut c_uchar,
    result_len: *mut c_ulong,
) -> c_int {
    let p1_byte = unsafe { slice::from_raw_parts(point1, len1 as usize) };
    let p2_byte = unsafe { slice::from_raw_parts(point2, len2 as usize) };
    let p3_byte = unsafe { slice::from_raw_parts_mut(result, *result_len as usize) };

    let ret = alt_bn128_add_internal(p1_byte, p2_byte, p3_byte);
    match ret {
        Err(e) => return e.code,
        Ok(_) => return 0 as c_int,
    }
}

fn alt_bn128_add_internal(
    p1_byte: &[u8],
    p2_byte: &[u8],
    p3_byte: &mut [u8],
) -> Result<(), PairingErr> {
    let p1 = deserialize_g1(p1_byte)?;
    let p2 = deserialize_g1(p2_byte)?;

    let p3 = p1 + p2;
    if let Some(p3_af) = AffineG1::from_jacobian(p3) {
        p3_af
            .x()
            .into_u256()
            .to_big_endian(&mut p3_byte[0..FP_SIZE])?;
        p3_af
            .y()
            .into_u256()
            .to_big_endian(&mut p3_byte[FP_SIZE..FP_SIZE * 2])?;
    }

    Ok(())
}

#[no_mangle]
pub extern "system" fn Java_org_aion_tetryon_AltBn128Jni_g1EcMul(
    env: JNIEnv,
    class: JClass,
    point_j: jbyteArray,
    scalar_j: jbyteArray,
) -> jbyteArray {
    let pt_byte = env.convert_byte_array(point_j).unwrap();
    let scalar_byte = env.convert_byte_array(scalar_j).unwrap();

    let mut p3_byte: [u8; FP_SIZE * 2] = [0; FP_SIZE * 2];
    let ret = alt_bn128_mul_internal(&pt_byte, &scalar_byte, &mut p3_byte);

    let output = env.byte_array_from_slice(&p3_byte).unwrap();
    output
}

#[no_mangle]
pub extern "C" fn alt_bn128_mul(
    point: *const c_uchar,
    len: c_ulong,
    scalar: *const c_uchar,
    scalar_len: c_ulong,
    result: *mut c_uchar,
    result_len: *mut c_ulong,
) -> c_int {
    let pt_byte = unsafe { slice::from_raw_parts(point, len as usize) };
    let scalar_byte = unsafe { slice::from_raw_parts(scalar, scalar_len as usize) };
    let p3_byte = unsafe { slice::from_raw_parts_mut(result, *result_len as usize) };

    let ret = alt_bn128_mul_internal(pt_byte, scalar_byte, p3_byte);
    match ret {
        Err(e) => return e.code,
        Ok(_) => return 0 as c_int,
    }
}

fn alt_bn128_mul_internal(
    pt_byte: &[u8],
    scalar_byte: &[u8],
    p3_byte: &mut [u8],
) -> Result<(), PairingErr> {
    let pt = deserialize_g1(pt_byte)?;
    let s = Fr::from_slice(&scalar_byte[0..FP_SIZE])?;

    let p3 = pt * s;
    if let Some(p3_af) = AffineG1::from_jacobian(p3) {
        p3_af
            .x()
            .into_u256()
            .to_big_endian(&mut p3_byte[0..FP_SIZE])?;
        p3_af
            .y()
            .into_u256()
            .to_big_endian(&mut p3_byte[FP_SIZE..FP_SIZE * 2])?;
    }
    // println!("{}", hex::encode(p3_byte));

    Ok(())
}

#[no_mangle]
pub extern "system" fn Java_org_aion_tetryon_AltBn128Jni_ecPair(
    env: JNIEnv,
    class: JClass,
    g1_point_list: jbyteArray,
    g2_point_list: jbyteArray,
) -> jboolean {
    let g1_list_byte = env.convert_byte_array(g1_point_list).unwrap();
    let g2_list_byte = env.convert_byte_array(g2_point_list).unwrap();

    let ret = alt_bn128_pair_internal(
        &g1_list_byte,
        &g2_list_byte,
        g1_list_byte.len() / (FP_SIZE * 2),
    );
    match ret {
        Err(e) => return 0 as jboolean,
        Ok(val) => val as jboolean,
    }
}

#[no_mangle]
pub extern "C" fn alt_bn128_pair(
    g1_point_list: *const c_uchar,
    g2_point_list: *const c_uchar,
    point_list_len: c_ulong,
    is_one: *mut c_int,
) -> c_int {
    let g1_list_byte = unsafe {
        slice::from_raw_parts(
            g1_point_list,
            (point_list_len as usize) * FP_SIZE * 2 as usize,
        )
    };
    let g2_list_byte = unsafe {
        slice::from_raw_parts(
            g2_point_list,
            (point_list_len as usize) * FP_SIZE * 4 as usize,
        )
    };

    let ret = alt_bn128_pair_internal(g1_list_byte, g2_list_byte, point_list_len as usize);
    match ret {
        Err(e) => return e.code,
        Ok(val) => {
            if val {
                unsafe { *is_one = 1 };
            } else {
                unsafe { *is_one = 0 };
            }
            0 as c_int
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_org_aion_tetryon_AltBn128Jni_myPair(
    env: JNIEnv,
    class: JClass,
    g1_point: jbyteArray,
    g2_point: jbyteArray,
) -> jbyteArray {
    let g1_byte = env.convert_byte_array(g1_point).unwrap();

    let g2_byte = env.convert_byte_array(g2_point).unwrap();
    let mut result_byte: [u8; FP_SIZE * 12] = [0; FP_SIZE * 12];

    let ret = my_alt_bn128_pair_internal(&g1_byte, &g2_byte, &mut result_byte).unwrap();
    // return ret as jboolean;
    let output = env.byte_array_from_slice(&result_byte).unwrap();

    output
}

#[no_mangle]
pub extern "system" fn Java_org_aion_tetryon_AltBn128Jni_gtPow(
    env: JNIEnv,
    class: JClass,
    gt_point: jbyteArray,
    exp: jbyteArray,
) -> jbyteArray {
    let gt_point = env.convert_byte_array(gt_point).unwrap();
    let exp_byte = env.convert_byte_array(exp).unwrap();

    let mut result_byte: [u8; FP_SIZE * 12] = [0; FP_SIZE * 12];

    let ret = my_alt_bn128_gt_pow_internal(&gt_point, &exp_byte, &mut result_byte).unwrap();
    // return ret as jboolean;
    let output = env.byte_array_from_slice(&result_byte).unwrap();

    output
}

fn my_alt_bn128_gt_pow_internal(
    gt_point_byte: &[u8],
    exp_byte: &[u8],
    result_byte: &mut [u8],
) -> Result<(), PairingErr> {
    let exp = Fr::from_slice(&exp_byte[0..FP_SIZE]).unwrap();

    let gt_point = deserialize_gt(gt_point_byte).unwrap();

    let gt_result = gt_point.pow(exp);

    // c0 to c11
    let c_i = gt_result.get_ci();
    let mut gt_buffer = vec![];
    let mut index: usize = 0;
    for item in c_i.iter() {
        let item_u256 = item.into_u256();
        gt_buffer.push(item_u256);
        item.into_u256()
            .to_big_endian(&mut result_byte[FP_SIZE * index..FP_SIZE * (index + 1)])
            .unwrap();
        // println!(
        //     "item {}",
        //     hex::encode(&result_byte[FP_SIZE * index..FP_SIZE * (index + 1)])
        // );
        index += 1;
    }

    Ok(())
}

fn my_alt_bn128_pair_internal(
    g1_byte: &[u8],
    g2_byte: &[u8],
    result_byte: &mut [u8],
) -> Result<(), PairingErr> {
    let g1_point = deserialize_g1(g1_byte)?;

    let g2_point = deserialize_g2(g2_byte)?;

    let result = pairing(g1_point, g2_point);
    // c0 to c11
    let c_i = result.get_ci();
    let mut gt_buffer = vec![];
    let mut index: usize = 0;
    for item in c_i.iter() {
        let item_u256 = item.into_u256();
        gt_buffer.push(item_u256);
        item.into_u256()
            .to_big_endian(&mut result_byte[FP_SIZE * index..FP_SIZE * (index + 1)])
            .unwrap();
        // println!(
        //     "item {}",
        //     hex::encode(&result_byte[FP_SIZE * index..FP_SIZE * (index + 1)])
        // );
        index += 1;
    }

    Ok(())
}

fn alt_bn128_pair_internal(
    g1_list_byte: &[u8],
    g2_list_byte: &[u8],
    point_list_len: usize,
) -> Result<bool, PairingErr> {
    let mut pair_list: Vec<(G1, G2)> = vec![];
    for i in 0..point_list_len {
        let g1_byte = &g1_list_byte[FP_SIZE * 2 * i..FP_SIZE * 2 * (i + 1)];
        let g2_byte = &g2_list_byte[FP_SIZE * 4 * i..FP_SIZE * 4 * (i + 1)];

        let g1 = deserialize_g1(g1_byte)?;
        let g2 = deserialize_g2(g2_byte)?;

        pair_list.push((g1, g2));
    }

    let gt = pairing_batch(&pair_list);

    Ok(gt == Gt::one())
}

#[no_mangle]
pub extern "C" fn call_test_from_c() {
    let rng = &mut rand::thread_rng();
    let alice_sk = Fr::random(rng);
    let bob_sk = Fr::random(rng);
    let carol_sk = Fr::random(rng);

    // Generate public keys in G1 and G2
    let (alice_pk1, alice_pk2) = (G1::one() * alice_sk, G2::one() * alice_sk);
    let (bob_pk1, bob_pk2) = (G1::one() * bob_sk, G2::one() * bob_sk);
    let (carol_pk1, carol_pk2) = (G1::one() * carol_sk, G2::one() * carol_sk);

    // Each party computes the shared secret
    let alice_ss = pairing(bob_pk1, carol_pk2).pow(alice_sk);
    let bob_ss = pairing(carol_pk1, alice_pk2).pow(bob_sk);
    let carol_ss = pairing(alice_pk1, bob_pk2).pow(carol_sk);

    assert!(alice_ss == bob_ss && bob_ss == carol_ss);
}

#[cfg(test)]
// extern crate hex;

mod tests {

    use super::*;

    #[test]
    fn it_works() {
        let rng = &mut rand::thread_rng();
        // Generate private keys
        let alice_sk = Fr::random(rng);
        let bob_sk = Fr::random(rng);
        let carol_sk = Fr::random(rng);

        // Generate public keys in G1 and G2
        let (alice_pk1, alice_pk2) = (G1::one() * alice_sk, G2::one() * alice_sk);
        let (bob_pk1, bob_pk2) = (G1::one() * bob_sk, G2::one() * bob_sk);
        let (carol_pk1, carol_pk2) = (G1::one() * carol_sk, G2::one() * carol_sk);

        // Each party computes the shared secret
        let alice_ss = pairing(bob_pk1, carol_pk2).pow(alice_sk);
        let bob_ss = pairing(carol_pk1, alice_pk2).pow(bob_sk);
        let carol_ss = pairing(alice_pk1, bob_pk2).pow(carol_sk);

        assert!(alice_ss == bob_ss && bob_ss == carol_ss);
    }

    #[test]
    fn serialize() {
        let rng = &mut rand::thread_rng();
        let alice_sk = Fr::random(rng);
        let (alice_pk1, alice_pk2) = (G1::one() * alice_sk, G2::one() * alice_sk);
        let alice_af_pk1 = AffineG1::from_jacobian(alice_pk1).unwrap();
        let alice_af_pk2 = AffineG2::from_jacobian(alice_pk2).unwrap();
        let mut buffer: [u8; 32] = [0; 32];
        alice_af_pk1.x().to_big_endian(&mut buffer).unwrap();
        alice_af_pk2.x().real().to_big_endian(&mut buffer).unwrap();
        alice_af_pk2
            .x()
            .imaginary()
            .to_big_endian(&mut buffer)
            .unwrap();
        assert!(buffer.len() == 32)
    }

    #[test]
    fn serialize_fr() {
        let rng = &mut rand::thread_rng();
        // Generate private keys
        let alice_sk = Fr::random(rng);
        let mut buffer: [u8; 32] = [0; 32];
        alice_sk.into_u256().to_big_endian(&mut buffer).unwrap();
        println!("{}", hex::encode(&buffer));
        let sk = Fr::from_slice(&buffer).unwrap();
        sk.into_u256().to_big_endian(&mut buffer).unwrap();
        println!("{}", hex::encode(&buffer));
        println!("{}", alice_sk == sk);
    }

    #[test]
    fn gen_test_case() {
        let rng = &mut rand::thread_rng();
        // Generate private keys
        let alice_sk = Fr::random(rng);
        let bob_sk = Fr::random(rng);

        // Generate public keys in G1 and G2
        let alice_pk1 = G1::one() * alice_sk;
        let bob_pk1 = G1::one() * bob_sk;

        let alice_af_pk1 = AffineG1::from_jacobian(alice_pk1).unwrap();
        let bob_af_pk1 = AffineG1::from_jacobian(bob_pk1).unwrap();

        let pk3 = alice_pk1 + bob_pk1;
        let af_pk3 = AffineG1::from_jacobian(pk3).unwrap();
        let mut buffer: [u8; 32] = [0; 32];
        alice_af_pk1.x().to_big_endian(&mut buffer).unwrap();
        println!("pk1.x {}", hex::encode(&buffer));
        alice_af_pk1.y().to_big_endian(&mut buffer).unwrap();
        println!("pk1.y {}", hex::encode(&buffer));
        bob_af_pk1.x().to_big_endian(&mut buffer).unwrap();
        println!("{}", hex::encode(&buffer));
        bob_af_pk1.y().to_big_endian(&mut buffer).unwrap();
        println!("{}", hex::encode(&buffer));
        af_pk3.x().to_big_endian(&mut buffer).unwrap();
        println!("{}", hex::encode(&buffer));
        af_pk3.y().to_big_endian(&mut buffer).unwrap();
        println!("{}", hex::encode(&buffer));

        let p4 = alice_pk1 * bob_sk;
        bob_sk.into_u256().to_big_endian(&mut buffer).unwrap();
        println!("scalar: {}", hex::encode(&buffer));

        let af_p4 = AffineG1::from_jacobian(p4).unwrap();
        af_p4.x().to_big_endian(&mut buffer).unwrap();
        println!("p.x {}", hex::encode(&buffer));
        af_p4.y().to_big_endian(&mut buffer).unwrap();
        println!("p.y {}", hex::encode(&buffer));

        assert!(true);
    }

    #[test]
    fn gen_pair_test() {
        let rng = &mut rand::thread_rng();
        // Generate private keys
        let alice_sk = Fr::random(rng);
        let bob_sk = Fr::random(rng);

        let (alice_pk1, _) = (G1::one() * alice_sk, G2::one() * alice_sk);
        let (_, bob_pk2) = (G1::one() * bob_sk, G2::one() * bob_sk);

        let mut vals = Vec::new();
        let alice_pk1_2 = -alice_pk1;
        vals.push((alice_pk1, bob_pk2));
        vals.push((alice_pk1_2, bob_pk2));
        let result = pairing_batch(&vals);

        let mut buffer: [u8; 32] = [0; 32];
        let alice_af_pk1 = AffineG1::from_jacobian(alice_pk1).unwrap();
        let alice_af_pk1_2 = AffineG1::from_jacobian(alice_pk1_2).unwrap();
        alice_af_pk1
            .x()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g1_1.x {}", hex::encode(&buffer));
        alice_af_pk1
            .y()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g1_1.y {}", hex::encode(&buffer));
        alice_af_pk1_2
            .x()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g1_2.x {}", hex::encode(&buffer));
        alice_af_pk1_2
            .y()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g1_2.y {}", hex::encode(&buffer));

        let bob_af_pk2 = AffineG2::from_jacobian(bob_pk2).unwrap();

        bob_af_pk2
            .x()
            .real()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2.x1 {}", hex::encode(&buffer));

        bob_af_pk2
            .x()
            .imaginary()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2.x2 {}", hex::encode(&buffer));
        bob_af_pk2
            .y()
            .real()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2.y1 {}", hex::encode(&buffer));
        bob_af_pk2
            .y()
            .imaginary()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2.y2 {}", hex::encode(&buffer));
        println!("{}", Gt::one() == result);
    }

    #[test]
    fn Gt_pow_test() {
        let rng = &mut rand::thread_rng();
        let alice_sk = Fr::from_str(
            "18097487326282793650237947474982649264364522469319914492172746413872781676",
        )
        .unwrap();
        let alice_pk1 = G1::one() * alice_sk;
        let gt = pairing(alice_pk1, G2::one());

        let exp = Fr::from_str("5").unwrap();
        let gt = gt.pow(exp);

        let gt_result = gt.pow(exp);
        // let gt_result = Gt(gt_result);

        println!("{}", gt_result == gt);
        let gt_result1 = gt_result.pow(exp.inverse().unwrap());
        println!("{}", gt_result1 == gt);

        let a = 1;
    }

    #[test]
    fn deserialize_gt_test() {
        let rng = &mut rand::thread_rng();
        let alice_sk = Fr::random(rng);

        let (alice_pk1, alice_pk2) = (G1::one() * alice_sk, G2::one() * alice_sk);

        let alice_ss = pairing(alice_pk1, alice_pk2);

        let c_i = alice_ss.get_ci();

        let mut temp: [[u8; 32]; 12] = [[0; 32]; 12];
        for i in 0..12 {
            let item = c_i[i];
            let item_u256 = item.into_u256();
            // gt_buffer.push(item_u256);
            item.into_u256().to_big_endian(&mut temp[i]).unwrap();
            println!("old gt:{}", hex::encode(temp[i]));
        }

        let result = [
            temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7], temp[8],
            temp[9], temp[10], temp[11],
        ]
        .concat();

        let gt_old = deserialize_gt(&result).unwrap();
        println!("{}", gt_old == alice_ss);
    }

    #[test]
    fn my_test() {
        let rng = &mut rand::thread_rng();
        let alice_sk = Fr::from_str(
            "18097487326282793650237947474982649264364522469319914492172746413872781676",
        )
        .unwrap();
        let alice_pk1 = G1::one() * alice_sk;
        let alice_pk1_real = AffineG1::from_jacobian(alice_pk1).unwrap();

        let alice_sk2 = Fr::from_str(
            "20390255904278144451778773028944684152769293537511418234311120800877067946",
        )
        .unwrap();
        let alice_pk2 = G2::one() * alice_sk2;
        let alice_pk2_real = AffineG2::from_jacobian(alice_pk2).unwrap();

        let mut buffer: [u8; 32] = [0; 32];
        let g1_x = alice_pk1_real
            .x()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();

        println!("g1_x: {}", hex::encode(&buffer));

        let g1_y = alice_pk1_real
            .y()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();

        println!("g1_y: {}", hex::encode(&buffer));

        let g2_1x = alice_pk2_real
            .x()
            .real()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2_1x: {}", hex::encode(&buffer));
        let g2_1y = alice_pk2_real
            .y()
            .real()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2_1y: {}", hex::encode(&buffer));

        let g2_2x = alice_pk2_real
            .x()
            .imaginary()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2_1y: {}", hex::encode(&buffer));

        let g2_2y = alice_pk2_real
            .y()
            .imaginary()
            .into_u256()
            .to_big_endian(&mut buffer)
            .unwrap();
        println!("g2_2y: {}", hex::encode(&buffer));

        let e_g1_g2 = pairing(alice_pk1, alice_pk2);

        let c_i = e_g1_g2.get_ci();
        let mut gt_buffer: [u8; 32] = [0; 32];
        let mut index: usize = 0;
        for item in c_i.iter() {
            let item_u256 = item.into_u256();

            item.to_big_endian(&mut gt_buffer).unwrap();

            println!("item {}", hex::encode(&gt_buffer));
            index += 1;
            // let hex_item = hex::encode(&buffer0);
            // // println!("item {}", buffer0);
        }

        // let target = (18047981096479337102146667749895076239629141801291955469384704565216104187314, 15666664160719503771597247415419223930649457344221068845696585378185259231782, 19104486138427766678685981088012332131195800219059514705455734667515973606461, 9981398918396711625955686259070817172664979929677830651988555105509994339171, 11896632435097590900354609446159654476372962698668471398580977554098318198563, 14221980458534454581231952169237170519081003872664907970911977130536153649527, 10342309967155150758354801395014075652225221643351782543638326396015198777712, 11621837078398424322606208174682557100993333529246445615493549022059226612768, 5236968339662193120507082558938968646636336333031399422938355946719863068135, 18078826069444667006861228825551021353661733895571187371353872321244928807011, 9227965260032410354414324195411397451806478640651098414622140162144550225774, 4565231055964369875954050676600907662092934946529352128407430527253758726453)

        let (alice_pk1, alice_pk2) = (G1::one() * alice_sk, G2::one() * alice_sk);

        let alice_ss = pairing(alice_pk1, alice_pk2);

        let alice_pk1 = AffineG1::from_jacobian(alice_pk1).unwrap();
        let alice_pk2 = AffineG2::from_jacobian(alice_pk2).unwrap();

        let mut temp: [u8; 32] = [0; 32];
        let mut temp10: [u8; 32] = [0; 32];

        let g1_byte_x = alice_pk1.x().into_u256().to_big_endian(&mut temp).unwrap();
        let g1_byte_y = alice_pk1
            .y()
            .into_u256()
            .to_big_endian(&mut temp10)
            .unwrap();

        let g1 = [temp, temp10].concat();

        let g1_old = deserialize_g1(&g1).unwrap();
        let result = (g1_old == G1::one() * alice_sk);

        let mut temp0: [u8; 32] = [0; 32]; // g2.x0
        let mut temp1: [u8; 32] = [0; 32]; // g2.x1
        let mut temp2: [u8; 32] = [0; 32]; // g2.y0
        let mut temp3: [u8; 32] = [0; 32]; // g2.y1

        alice_pk2
            .x()
            .real()
            .into_u256()
            .to_big_endian(&mut temp0)
            .unwrap();
        alice_pk2
            .x()
            .imaginary()
            .into_u256()
            .to_big_endian(&mut temp1)
            .unwrap();

        alice_pk2
            .y()
            .real()
            .into_u256()
            .to_big_endian(&mut temp2)
            .unwrap();

        alice_pk2
            .y()
            .imaginary()
            .into_u256()
            .to_big_endian(&mut temp3)
            .unwrap();

        let mut g2 = [temp0, temp1, temp2, temp3].concat();

        let g2_old = G2::one() * alice_sk;

        let g2_new = deserialize_g2(&g2).unwrap();

        let result = (g2_old == g2_new);

        let mut buffer0: [u8; 32 * 12] = [0; 32 * 12];
        let mut result_byte: [u8; 32 * 16] = [0; 32 * 16];
        my_alt_bn128_pair_internal(&g1, &g2, &mut result_byte);
        let index: usize = 12;
        println!("------------------------");
        let mut gt_buffer = vec![];
        let mut buff: [u8; 32] = [0; 32];
        for i in 0..index {
            let c = U256::from_slice(&result_byte[i * FP_SIZE..(i + 1) * FP_SIZE])
                .unwrap()
                .to_big_endian(&mut buff);
            println!("item {}", hex::encode(&buff));
            gt_buffer.push(c);
        }
        // gt_buffer.reverse();
        // let c0 = bn::fields::Fq::new(gt_buffer[0]).unwrap();
        // let c1 = bn::fields::Fq::new(gt_buffer[1]).unwrap();
        // let c2 = bn::fields::Fq::new(gt_buffer[2]).unwrap();
        // let c3 = bn::fields::Fq::new(gt_buffer[3]).unwrap();
        // let c4 = bn::fields::Fq::new(gt_buffer[4]).unwrap();
        // let c5 = bn::fields::Fq::new(gt_buffer[5]).unwrap();
        // let c6 = bn::fields::Fq::new(gt_buffer[6]).unwrap();
        // let c7 = bn::fields::Fq::new(gt_buffer[7]).unwrap();
        // let c8 = bn::fields::Fq::new(gt_buffer[8]).unwrap();
        // let c9 = bn::fields::Fq::new(gt_buffer[9]).unwrap();
        // let c10 = bn::fields::Fq::new(gt_buffer[10]).unwrap();
        // let c11 = bn::fields::Fq::new(gt_buffer[11]).unwrap();

        // let fq6c0_fq2c0 = bn::fields::Fq2::new(c0, c1);
        // let fq6c0_fq2c1 = bn::fields::Fq2::new(c2, c3);
        // let fq6c0_fq2c2 = bn::fields::Fq2::new(c4, c5);

        // let fq6c1_fq2c0 = bn::fields::Fq2::new(c6, c7);
        // let fq6c1_fq2c1 = bn::fields::Fq2::new(c8, c9);
        // let fq6c1_fq2c2 = bn::fields::Fq2::new(c10, c11);

        // let fq6c0 = bn::fields::Fq6::new(fq6c0_fq2c0, fq6c0_fq2c1, fq6c0_fq2c2);
        // let fq6c1 = bn::fields::Fq6::new(fq6c1_fq2c0, fq6c1_fq2c1, fq6c1_fq2c2);

        // let fq12 = bn::fields::Fq12::new(fq6c0, fq6c1);
        // println!("{}", Gt::new(fq12) == alice_ss);

        // let mut a = 1;
        // a += 1;
    }

    #[test]
    fn my_pair_test() {
        let rng = &mut rand::thread_rng();
        let alice_sk = Fr::random(rng);
        let bob_sk = Fr::random(rng);

        let (alice_pk1, alice_pk2) = (G1::one() * alice_sk, G2::one() * alice_sk);
        let (bob_pk1, bob_pk2) = (G1::one() * bob_sk, G2::one() * bob_sk);

        let alice_ss = pairing(alice_pk1, alice_pk2);
        println!("pow before {:?}", alice_ss.0);
        let alice_ss = alice_ss.pow(alice_sk);
        println!("pow afeter {:?}", alice_ss.0);

        let gt_one = alice_ss;
        // let gt_one = Gt::one();
        // c0 to c11
        let c_i = gt_one.get_ci();
        let mut gt_buffer = vec![];
        let mut buffer0: [u8; 32] = [0; 32];

        for item in c_i.iter() {
            let item_u256 = item.into_u256();
            gt_buffer.push(item_u256);
            item.into_u256().to_big_endian(&mut buffer0).unwrap();
            let hex_item = hex::encode(&buffer0);
            // println!("item {}", buffer0);
            println!("item {}", hex::encode(&buffer0));
        }
        // gt_buffer.reverse();
        let c0 = bn::fields::Fq::new(gt_buffer[0]).unwrap();
        let c1 = bn::fields::Fq::new(gt_buffer[1]).unwrap();
        let c2 = bn::fields::Fq::new(gt_buffer[2]).unwrap();
        let c3 = bn::fields::Fq::new(gt_buffer[3]).unwrap();
        let c4 = bn::fields::Fq::new(gt_buffer[4]).unwrap();
        let c5 = bn::fields::Fq::new(gt_buffer[5]).unwrap();
        let c6 = bn::fields::Fq::new(gt_buffer[6]).unwrap();
        let c7 = bn::fields::Fq::new(gt_buffer[7]).unwrap();
        let c8 = bn::fields::Fq::new(gt_buffer[8]).unwrap();
        let c9 = bn::fields::Fq::new(gt_buffer[9]).unwrap();
        let c10 = bn::fields::Fq::new(gt_buffer[10]).unwrap();
        let c11 = bn::fields::Fq::new(gt_buffer[11]).unwrap();

        let fq6c0_fq2c0 = bn::fields::Fq2::new(c0, c1);
        let fq6c0_fq2c1 = bn::fields::Fq2::new(c2, c3);
        let fq6c0_fq2c2 = bn::fields::Fq2::new(c4, c5);

        let fq6c1_fq2c0 = bn::fields::Fq2::new(c6, c7);
        let fq6c1_fq2c1 = bn::fields::Fq2::new(c8, c9);
        let fq6c1_fq2c2 = bn::fields::Fq2::new(c10, c11);

        let fq6c0 = bn::fields::Fq6::new(fq6c0_fq2c0, fq6c0_fq2c1, fq6c0_fq2c2);
        let fq6c1 = bn::fields::Fq6::new(fq6c1_fq2c0, fq6c1_fq2c1, fq6c1_fq2c2);

        let fq12 = bn::fields::Fq12::new(fq6c0, fq6c1);
        println!("{}", Gt::new(fq12) == alice_ss);
    }

    #[test]
    fn zero_test() {
        let buffer: [u8; 32] = [0; 32];
        let s = Fr::from_slice(&buffer).unwrap();
        let ret = G1::one() * s;
        let af_ret = AffineG1::from_jacobian(ret);
        assert!(af_ret.is_none());
    }
}
