# tenkaichi-anm-bone-replacer
A modding tool for the PS2 version of Budokai Tenkaichi 2 or 3 that can **edit ANM files from those games**, by **taking ANY specified bone contents of ANMs** located in a source folder, and **assigning them** to ANMs located in a destination folder.

For the program to work as intended, the ANM files in both source & destination folders **do not necessarily need to have identical file names**.

However, the **number of ANMs in both folders must be the same**, and to match a source ANM with a destination ANM, the ANMs in both folders must be sorted such that **the intended order of the ANMs is the same**.
A good way of achieving this is to number the ANMs in both folders the same way.

For example: ``SRC01.anm -> DST01.anm``, ``SRC02.anm -> DST02.anm``, and so on...

# Usage
## CLI
![bafkreiazigchegufsbozgwmbkjdll67ht2x4ctv3wypivqaw4kn3cy5hv4](https://github.com/user-attachments/assets/7dea6a44-73d2-4889-b071-1e01787dda10)

![bafkreifupw6oy4z6dqaptvwqunv2cqtlwqgdn6krwgwjocme7ptry4i4qy](https://github.com/user-attachments/assets/10ff6d5e-20ce-4316-951e-7895e17af2e5)

## GUI
<img width="533" height="393" alt="image" src="https://github.com/user-attachments/assets/4aa48131-3979-4167-ad31-15525c4ad2c2" />

<img width="512" height="256" alt="image" src="https://github.com/user-attachments/assets/c553d63e-4942-43ce-ab47-b20eb31fa323" />

<img width="528" height="425" alt="image" src="https://github.com/user-attachments/assets/55144d15-c574-47e2-9f77-0af075f7490f" />

<img width="539" height="571" alt="image" src="https://github.com/user-attachments/assets/34d79db2-1ec5-49ed-bd61-efd01d0d2b16" />

<img width="529" height="585" alt="image" src="https://github.com/user-attachments/assets/d060864c-a3c8-47f3-92e9-e2c16a184f06" />

As of version 1.4, support for replacement of special bone contents has been added.

<img width="526" height="276" alt="image" src="https://github.com/user-attachments/assets/76a4e7b9-87ed-4d73-acc0-bc0637570208" />

<img width="512" height="189" alt="image" src="https://github.com/user-attachments/assets/423f9f27-fbb0-4194-9c9e-6119cfc18b9e" />

To explain these special bones better:
* The ``OPTION`` bones' purpose vary from model to model. Devilman for example makes use of bones OPTION01-OPTION08 for his wings.
* The ``EQUIPMENT`` bones are accessories meant to be equipped on the character's left or right hands. Examples include (but aren't limited to): potara earrings, grenades, swords.
* The ``BIND_EQUIPMENT`` bones are meant to access the ``EQUIPMENT`` bones, by disappearing entirely whenever the ``EQUIPMENT`` bone is shown.

# Results
![bafkreiemnumet4g2qjniadpuq66c5le6xb5maxemezh6hi5532sqnwmja4](https://github.com/user-attachments/assets/8bd8ba2b-f84d-4a8c-ae97-c6af0286fde7)

![bafkreigp7hn4go6kc3wtzojruk733mr7znkn4xbakk35rfqftuimxv2eo4](https://github.com/user-attachments/assets/b77196d5-b402-4505-8f02-1a7ceaff88aa)

<img width="809" height="730" alt="image" src="https://github.com/user-attachments/assets/fb317ec5-61cd-4b41-8202-64808726de08" />

<img width="808" height="730" alt="image" src="https://github.com/user-attachments/assets/5f03b499-4c70-4b69-a3a5-aa88c360b03b" />

<img width="805" height="727" alt="image" src="https://github.com/user-attachments/assets/9eada7bb-c414-4eea-9ba6-ebd090fb5b17" />

<img width="807" height="726" alt="image" src="https://github.com/user-attachments/assets/772d4264-c313-434d-81e1-7fd9bcaa355b" />

https://github.com/user-attachments/assets/5ac98e89-5a5e-456b-a9ea-5eb6595ea022

